package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MutableMap}
import org.neo4j.graphdb._
import org.neo4j.graphdb.Direction._
import java.util.{UUID, Locale}
import java.util.Locale._
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.CharacterRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.CrewRelationshipType
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.DigitalContainerRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.EntityRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.MotionPictureRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.MovieCatalogDbConstants._
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.PropertyManager._

private[neo4j] class NodeManager(db: GraphDatabaseService) {

  Validate.notNull(db)

  private final val DbMgr = DatabaseManager(db)
  private final val IdxMgr = IndexManager(db)

  def createNodeFrom(e: Entity)(implicit tx: Transaction, l: Locale = US): Node = lock(e) { withExistenceCheck(e) {
    val n = DbMgr.createNodeFor(e)
    e match {
      case c: Cast => connectNodeToSubreferenceNode(connectNodeToSubreferenceNode(setNodePropertiesFrom(n, c), c.getClass), classOf[Cast])
      case c: Character => connectNodeToSubreferenceNode(setProperties(n, c), classOf[Character])
      case dc: DigitalContainer => connectNodeToSubreferenceNode(setProperties(n, dc), classOf[DigitalContainer])
      case g: Genre => connectNodeToSubreferenceNode(setProperties(n, g, l), classOf[Genre])
      case m: MotionPicture => connectNodeToSubreferenceNode(connectNodeToSubreferenceNode(setProperties(n, m), m.getClass), classOf[MotionPicture])
      case p: Person => connectNodeToSubreferenceNode(setProperties(n, p), classOf[Person])
      case s: Soundtrack => connectNodeToSubreferenceNode(setProperties(n, s, l), classOf[Soundtrack])
      case s: Subtitle => connectNodeToSubreferenceNode(setProperties(n, s, l), classOf[Subtitle])
    }
  }}

  def updateNodeOf(e: Entity)(implicit tx: Transaction, l: Locale = US): Node = lock(e) { withExistenceCheck(e) {
    val n = DbMgr.getNodeOf(e)
    e match {
      case c: Cast => setNodePropertiesFrom(n, c)
      case c: Character => setProperties(n, c)
      case dc: DigitalContainer => setProperties(n, dc)
      case g: Genre => setProperties(n, g, l)
      case m: MotionPicture => setProperties(n, m)
      case p: Person => setProperties(n, p)
      case s: Soundtrack => setProperties(n, s, l)
      case s: Subtitle => setProperties(n, s, l)
    }
  }}

  def deleteNodeOf(e: Entity)(implicit tx: Transaction) {
    lock(e) {
      val n = DbMgr.getNodeOf(e)
      if (n.hasRelationship(INCOMING)) throw new IllegalStateException("%s is referenced by other entities".format(e))
      if (!hasExpectedVersion(n, e.version)) throw new IllegalStateException("%s is out of date".format(e))
      n.getRelationships(OUTGOING).asScala.foreach(_.delete())
      n.delete()
      n
    }
  }

  def getNodesOfType[A <: Entity](c: Class[A]): Iterator[Node] = DbMgr.getSubreferenceNode(c).getRelationships(IsA, INCOMING).iterator.asScala.map(_.getStartNode)

  private def lock(e: Entity)(dbOp: => Node)(implicit tx: Transaction): Node = {
    tx.acquireWriteLock(DbMgr.getSubreferenceNode(e.getClass))
    dbOp
  }

  private def withExistenceCheck(e: Entity)(dbOp: => Node) = {
    val node = IdxMgr.lookUpExact(e)
    if (node.isEmpty || Some(getUuid(node.get)) == e.id) dbOp else throw new IllegalArgumentException("Entity %s already exists".format(e))
  }

  private def connectNodeToSubreferenceNode[A <: Entity](n: Node, c: Class[A]): Node = { n.createRelationshipTo(DbMgr.getSubreferenceNode(c), IsA); n }

  private def setNodePropertiesFrom(n: Node, c: Cast): Node = {
    n.getRelationships(CrewRelationshipType.forClass(c.getClass), OUTGOING).asScala.foreach(_.delete())
    n.getRelationships(Played, OUTGOING).asScala.foreach(_.delete())
    n.getRelationships(AppearedIn, OUTGOING).asScala.foreach(_.delete())
    n.createRelationshipTo(DbMgr.getNodeOf(c.person), CrewRelationshipType.forClass(c.getClass))
    n.createRelationshipTo(DbMgr.getNodeOf(c.character), Played)
    n.createRelationshipTo(DbMgr.getNodeOf(c.motionPicture), AppearedIn)
    setVersion(n, c)
    IdxMgr.index(n, c)
    n
  }

  private def setProperties(n: Node, c: Character): Node = {
    setString(n, CharacterName, c.name)
    setString(n, CharacterCreator, c.creator)
    setLocalDate(n, CharacterCreationDate, c.creationDate)
    setVersion(n, c)
    IdxMgr.index(n, c)
    n
  }

  private def setProperties(n: Node, dc: DigitalContainer): Node = {
    n.getRelationships(WithContent, OUTGOING).asScala.foreach(_.delete())
    n.getRelationships(WithSoundtrack, OUTGOING).asScala.foreach(_.delete())
    n.getRelationships(WithSubtitle, OUTGOING).asScala.foreach(_.delete())
    n.createRelationshipTo(DbMgr.getNodeOf(dc.motionPicture), WithContent)
    dc.soundtracks.map(DbMgr.getNodeOf(_)).foreach(n.createRelationshipTo(_, WithSoundtrack))
    dc.subtitles.map(DbMgr.getNodeOf(_)).foreach(n.createRelationshipTo(_, WithSubtitle))
    setVersion(n, dc)
    IdxMgr.index(n, dc)
    n
  }

  private def setProperties(n: Node, g: Genre, l: Locale): Node = {
    if (g.name.isDefined && (g.name.get.locale != l)) throw new IllegalStateException("Genre name locale %s does not match the current locale %s".format(g.name.get.locale, l))
    setString(n, GenreCode, g.code)
    if (g.name.isDefined) addOrReplaceLocalizedText(n, GenreName, g.name.get) else deleteLocalizedText(n, GenreName, l)
    setVersion(n, g)
    IdxMgr.index(n, g)
    n
  }

  private def setProperties(n: Node, m: MotionPicture): Node = {
    n.getRelationships(HasGenre, OUTGOING).asScala.foreach(_.delete())
    setLocalizedText(n, MovieOriginalTitle, m.originalTitle)
    setLocalizedText(n, MovieLocalizedTitles, m.localizedTitles)
    m.genres.map(DbMgr.getNodeOf(_)).foreach(n.createRelationshipTo(_, HasGenre))
    setDuration(n, MovieRuntime, m.runtime)
    setLocalDate(n, MovieReleaseDate, m.releaseDate)
    setVersion(n, m)
    IdxMgr.index(n, m)
    n
  }

  private def setProperties(n: Node, p: Person): Node = {
    setString(n, PersonName, p.name)
    setString(n, PersonGender, p.gender.toString)
    setLocalDate(n, PersonDateOfBirth, p.dateOfBirth)
    setString(n, PersonPlaceOfBirth, p.placeOfBirth)
    setVersion(n, p)
    IdxMgr.index(n, p)
    n
  }

  private def setProperties(n: Node, s: Soundtrack, l: Locale): Node = {
    if (s.languageName.isDefined && (s.languageName.get.locale != l)) throw new IllegalStateException("Soundtrack language name locale %s does not match the current locale %s".format(s.languageName.get.locale, l))
    if (s.formatName.isDefined && (s.formatName.get.locale != l)) throw new IllegalStateException("Soundtrack format name locale %s does not match the current locale %s".format(s.formatName.get.locale, l))
    setString(n, SoundtrackLanguageCode, s.languageCode)
    setString(n, SoundtrackFormatCode, s.formatCode)
    if (s.languageName.isDefined) addOrReplaceLocalizedText(n, SoundtrackLanguageName, s.languageName.get) else deleteLocalizedText(n, SoundtrackLanguageName, l)
    if (s.formatName.isDefined) addOrReplaceLocalizedText(n, SoundtrackFormatName, s.formatName.get) else deleteLocalizedText(n, SoundtrackFormatName, l)
    setVersion(n, s)
    IdxMgr.index(n, s)
    n
  }

  private def setProperties(n: Node, s: Subtitle, l: Locale): Node = {
    if (s.languageName.isDefined && (s.languageName.get.locale != l)) throw new IllegalStateException("Subtitle language name locale %s does not match the current locale %s".format(s.languageName.get.locale, l))
    setString(n, SubtitleLanguageCode, s.languageCode)
    if (s.languageName.isDefined) addOrReplaceLocalizedText(n, SubtitleLanguageName, s.languageName.get) else deleteLocalizedText(n, SubtitleLanguageName, l)
    setVersion(n, s)
    IdxMgr.index(n, s)
    n
  }

  private def setVersion(n: Node, e: Entity) {
    if (e.id.isDefined && !hasExpectedVersion(n, e.version)) throw new IllegalStateException("%s is out of date".format(e))
    setLong(n, Version, if (e.id.isEmpty) e.version else e.version + 1)
  }

  private def hasExpectedVersion(n: Node, v: Long) = hasLong(n, Version) && getLong(n, Version) == v
}

private[neo4j] object NodeManager {

  private final val Instances = MutableMap.empty[GraphDatabaseService, NodeManager]

  def apply(db: GraphDatabaseService): NodeManager = {
    if (!Instances.contains(db)) {
      Instances += db -> new NodeManager(db)
    }
    Instances(db)
  }
}
