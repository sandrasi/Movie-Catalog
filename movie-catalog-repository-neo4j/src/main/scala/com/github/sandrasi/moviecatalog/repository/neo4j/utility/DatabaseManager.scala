package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MutableMap}
import org.neo4j.graphdb.{GraphDatabaseService, Node, NotFoundException}
import org.neo4j.graphdb.Direction._
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.Entity
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.EntityRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.SubreferenceRelationshipType
import com.github.sandrasi.moviecatalog.repository.neo4j.transaction.TransactionSupport
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.MovieCatalogDbConstants._
import org.apache.lucene.search.{TermQuery, BooleanQuery}
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause.Occur._
import java.util.UUID

private[neo4j] class DatabaseManager(db: GraphDatabaseService) extends TransactionSupport {

  Validate.notNull(db)

  private final val IdxMgr = db.index()
  private final val NodeIdIndex = IdxMgr.forNodes("Node")

  def getNodeOf(e: Entity) = try {
    def lookUpNode(id: UUID) = {
      val query = new BooleanQuery()
      query.add(new TermQuery(new Term(Uuid, id.toString)), MUST)
      Option(NodeIdIndex.query(query).getSingle)
    }
    e.id.flatMap(lookUpNode(_)) match {
      case Some(n) => if (isNodeOfType(n, e.getClass)) n else throw new ClassCastException("Node [id: %d] is not of type %s".format(n.getId, e.getClass.getName))
      case None => throw new IllegalStateException("%s is not in the database".format(e))
    }
  } catch {
    case _: NoSuchElementException => throw new IllegalStateException("more than one node have the id %s".format(e.id.get))
  }

  def createNodeFor(e: Entity): Node = if (e.id.isEmpty) {
    val node = db.createNode()
    val uuid = UUID.randomUUID.toString
    node.setProperty(Uuid, uuid)
    NodeIdIndex.add(node, Uuid, uuid)
    node
  } else throw new IllegalStateException("Entity %s already has an id: %s".format(e, e.id.get))

  def getSubreferenceNode[A <: Entity](c: Class[A]) = db.getNodeById(getSubreferenceNodeId(c))

  def isSubreferenceNode(n: Node): Boolean = SubreferenceRelationshipType.values.exists(v => getOrCreateSubreferenceNodeId(v) == n.getId)

  private def getSubreferenceNodeId[A <: Entity](c: Class[A]): Long = try {
    getOrCreateSubreferenceNodeId(SubreferenceRelationshipType.forClass(c))
  } catch {
    case _: NoSuchElementException => throw new IllegalArgumentException("Unsupported entity type %s".format(c.getName))
  }

  private def getOrCreateSubreferenceNodeId(relType: SubreferenceRelationshipType): Long = {
    val rel = db.getReferenceNode.getSingleRelationship(relType, OUTGOING)
    val srn = if (rel == null) createSubreferenceNodeForRelationshipType(relType) else rel.getEndNode
    srn.getId
  }

  private def createSubreferenceNodeForRelationshipType(relType: SubreferenceRelationshipType) = transaction(db) {
    val srn = db.createNode()
    srn.setProperty(SubreferenceNodeClassName, relType.name)
    db.getReferenceNode.createRelationshipTo(srn, relType)
    srn
  }

  def isNodeOfType[A <: Entity](n: Node, entityType: Class[A]) = n.getRelationships(IsA, OUTGOING).asScala.view.map(_.getEndNode.getId).exists(_ == getSubreferenceNodeId(entityType))
}

private[neo4j] object DatabaseManager {

  private final val Instances = MutableMap.empty[GraphDatabaseService, DatabaseManager]

  def apply(db: GraphDatabaseService): DatabaseManager = {
    if (!Instances.contains(db)) {
      Instances += db -> new DatabaseManager(db)
    }
    Instances(db)
  }
}
