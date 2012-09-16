package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MutableMap}
import org.neo4j.graphdb.{GraphDatabaseService, Node, NotFoundException}
import org.neo4j.graphdb.Direction._
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.EntityRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.SubreferenceRelationshipType
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.SubreferenceRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.transaction.TransactionSupport
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.MovieCatalogDbConstants._

private[utility] class DatabaseManager(db: GraphDatabaseService) extends TransactionSupport {

  Validate.notNull(db)

  def getNodeOf(e: VersionedLongIdEntity) = try {
    val node = if (e.id != None) db.getNodeById(e.id.get) else throw new IllegalStateException("%s is not in the database".format(e))
    if (isNodeOfType(node, e.getClass)) node else throw new ClassCastException("Node [id: %d] is not of type %s".format(e.id.get, e.getClass.getName))
  } catch {
    case _: NotFoundException => throw new IllegalStateException("%s is not in the database".format(e))
  }

  def createNodeFor(e: VersionedLongIdEntity): Node = if (e.id == None) db.createNode() else throw new IllegalStateException("Entity %s already has an id: %d".format(e, e.id.get))

  def getSubreferenceNode[A <: VersionedLongIdEntity](c: Class[A]) = db.getNodeById(getSubreferenceNodeId(c))

  def isSubreferenceNode(n: Node): Boolean = SubreferenceRelationshipType.values.exists(v => getOrCreateSubreferenceNodeId(v.asInstanceOf[SubreferenceRelationshipType]) == n.getId)

  private def getSubreferenceNodeId[A <: VersionedLongIdEntity](c: Class[A]): Long = try {
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

  def isNodeOfType[A <: VersionedLongIdEntity](n: Node, entityType: Class[A]) = n.getRelationships(IsA, OUTGOING).asScala.view.map(_.getEndNode.getId).exists(_ == getSubreferenceNodeId(entityType))
}

private[utility] object DatabaseManager {

  private final val Instances = MutableMap.empty[GraphDatabaseService, DatabaseManager]

  def apply(db: GraphDatabaseService): DatabaseManager = {
    if (!Instances.contains(db)) {
      Instances += db -> new DatabaseManager(db)
    }
    Instances(db)
  }
}
