package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MutableMap}
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause.Occur.MUST
import org.apache.lucene.search.{BooleanQuery, NumericRangeQuery, Query, TermQuery}
import org.neo4j.index.lucene.ValueContext
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.AbstractCast
import com.github.sandrasi.moviecatalog.domain.entities.core.Character
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.CharacterRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.FilmCrewRelationshipType
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.MovieCatalogDbConstants._
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.PropertyManager._
import org.neo4j.graphdb._
import org.neo4j.graphdb.Direction._

private[utility] class UniqueNodeFactory(db: GraphDatabaseService) {

  Validate.notNull(db)

  private final val SubrefNodeSupp = SubreferenceNodeSupport(db)

  private final val IdxMgr = db.index()
  private final val CastIndex = IdxMgr.forRelationships("Cast")
  private final val CharacterIndex = IdxMgr.forNodes("Characters")
  
  def createNodeFrom(e: VersionedLongIdEntity)(implicit tx: Transaction): Node = e match {
    case ac: AbstractCast => lock(ac) { setNodePropertiesFrom(createNode(ac), ac) }
    case c: Character => lock(c) { setNodePropertiesFrom(createNode(c), c) }
    case _ => throw new IllegalArgumentException("Unsupported entity type: %s".format(e.getClass.getName))
  }

  private def lock(e: VersionedLongIdEntity)(dbOp: => Node)(implicit tx: Transaction): Node = {
    tx.acquireWriteLock(lockNodeFor(e))
    dbOp
  }

  private def lockNodeFor(e: VersionedLongIdEntity) = db.getNodeById(SubrefNodeSupp.getSubrefNodeIdFor(e.getClass))

  private def createNode(e: VersionedLongIdEntity): Node = if (e.id == None) db.createNode() else throw new IllegalStateException("Entity %s already has an id: %d".format(e, e.id.get))

  private def setNodePropertiesFrom(n: Node, ac: AbstractCast): Node = withExistenceCheck(ac) {
    n.getRelationships(FilmCrewRelationshipType.forClass(ac.getClass), OUTGOING).asScala.foreach(_.delete())
    n.getRelationships(Played, OUTGOING).asScala.foreach(_.delete())
    n.getRelationships(AppearedIn, OUTGOING).asScala.foreach(_.delete())
    n.createRelationshipTo(getNode(ac.person), FilmCrewRelationshipType.forClass(ac.getClass))
    n.createRelationshipTo(getNode(ac.character), Played)
    n.createRelationshipTo(getNode(ac.motionPicture), AppearedIn)
    setVersion(n, ac)
    index(n, ac)
    n
  }

  private def setNodePropertiesFrom(n: Node, c: Character): Node = withExistenceCheck(c) {
    setString(n, CharacterName, c.name)
    setString(n, CharacterDiscriminator, c.discriminator)
    setVersion(n, c)
    index(n, c)
    n
  }

  private def setVersion(n: Node, e: VersionedLongIdEntity) {
    if (e.id != None && !hasExpectedVersion(n, e.version)) throw new IllegalStateException("%s is out of date".format(e))
    setLong(n, Version, if (e.id == None) e.version else e.version + 1)
  }

  private def hasExpectedVersion(n: Node, v: Long) = hasLong(n, Version) && getLong(n, Version) == v
  
  private def index(n: Node, ac: AbstractCast) {
    CastIndex.get("type", FilmCrewRelationshipType.forClass(ac.getClass), n, null).iterator.asScala.foreach(CastIndex.remove(_))
    CastIndex.get("type", Played.name, n, null).iterator.asScala.foreach(CastIndex.remove(_))
    CastIndex.get("type", AppearedIn.name, n, null).iterator.asScala.foreach(CastIndex.remove(_))
    CastIndex.add(n.getSingleRelationship(FilmCrewRelationshipType.forClass(ac.getClass), OUTGOING), "type", FilmCrewRelationshipType.forClass(ac.getClass).name)
    CastIndex.add(n.getSingleRelationship(Played, OUTGOING), "type", Played.name)
    CastIndex.add(n.getSingleRelationship(AppearedIn, OUTGOING), "type", AppearedIn.name)
  }

  private def index(n: Node, c: Character) {
    CharacterIndex.remove(n)
    CharacterIndex.add(n, CharacterName, c.name)
    CharacterIndex.add(n, CharacterDiscriminator, c.discriminator)
  }

  private def withExistenceCheck(e: VersionedLongIdEntity)(dbOp: => Node) = if (!exists(e)) dbOp else throw new IllegalArgumentException("Entity %s already exists in the repository".format(e))

  private def exists(e: VersionedLongIdEntity): Boolean = e match {
    case ac: AbstractCast => exists(ac)
    case c: Character => exists(c)
    case _ => throw new IllegalArgumentException("Unsupported entity type: %s".format(e.getClass.getName))
  }
  
  private def exists(ac: AbstractCast): Boolean = {
    val castsWithSamePerson = CastIndex.get("type", FilmCrewRelationshipType.forClass(ac.getClass).name, null, getNode(ac.person)).iterator.asScala.map(_.getStartNode).toSet
    val castsWithSameCharacter = CastIndex.get("type", Played.name, null, getNode(ac.character)).iterator.asScala.map(_.getStartNode).toSet
    val castsWithSameMotionPicture = CastIndex.get("type", AppearedIn.name, null, getNode(ac.motionPicture)).iterator.asScala.map(_.getStartNode).toSet
    (castsWithSamePerson & castsWithSameCharacter & castsWithSameMotionPicture).size > 0
  }

  private def exists(c: Character): Boolean = {
    val query = new BooleanQuery()
    query.add(new TermQuery(new Term(CharacterName, c.name)), MUST)
    query.add(new TermQuery(new Term(CharacterDiscriminator, c.discriminator)), MUST)
    val indexHits = CharacterIndex.query(query)
    indexHits.getSingle != null
  }

    //case l: Long => NumericRangeQuery.newLongRange(k, l, l, true, true)
  // TODO (sandrasi): acquire a read lock on the node
  private def getNode(e: VersionedLongIdEntity) = try {
    val node = if (e.id != None) db.getNodeById(e.id.get) else throw new IllegalStateException("%s is not in the database".format(e))
    if (SubrefNodeSupp.isNodeOfType(node, e.getClass)) node else throw new ClassCastException("Node [id: %d] is not of type %s".format(e.id.get, e.getClass.getName))
  } catch {
    case _: NotFoundException => throw new IllegalStateException("%s is not in the database".format(e))
  }
}

private[utility] object UniqueNodeFactory {

  private final val Instances = MutableMap.empty[GraphDatabaseService, UniqueNodeFactory]

  def apply(db: GraphDatabaseService): UniqueNodeFactory = {
    if (!Instances.contains(db)) {
      Instances += db -> new UniqueNodeFactory(db)
    }
    Instances(db)
  }
}
