package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.mutable.{Map => MutableMap}
import org.neo4j.graphdb.Direction.OUTGOING
import org.neo4j.graphdb.{GraphDatabaseService, Node}
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.{AbstractCast, Actor, Actress}
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.EntityRelationshipType.IsA
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.SubreferenceRelationshipType
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.SubreferenceRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.transaction.TransactionSupport

private[neo4j] class SubreferenceNodeSupport private (db: GraphDatabaseService) extends MovieCatalogGraphPropertyNames with TransactionSupport {

  Validate.notNull(db)
  
  private final val ClassActor = classOf[Actor]
  private final val ClassActress = classOf[Actress]

  def isSubreferenceNode(n: Node): Boolean = SubreferenceRelationshipType.values.exists(v => getSubreferenceNodeIdOfRelationship(v.asInstanceOf[SubreferenceRelationshipType]) == n.getId)

  def getSubrefNodeIdFor[A <: VersionedLongIdEntity](c: Class[A]): Long = try {
    getSubreferenceNodeIdOfRelationship(SubreferenceRelationshipType.forClass(mapClassToSubreferenceRelationshipTypeClass(c)))
  } catch {
    case e: NoSuchElementException => throw new IllegalArgumentException("Unsupported entity type %s".format(c.getName))
  }

  private def mapClassToSubreferenceRelationshipTypeClass[A <: VersionedLongIdEntity](c: Class[A]) = c match {
    case ClassActor | ClassActress => classOf[AbstractCast]
    case _ => c
  }

  private def getSubreferenceNodeIdOfRelationship(relType: SubreferenceRelationshipType): Long = {
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

  def isNodeOfType[A <: VersionedLongIdEntity](n: Node, entityType: Class[A]) = {
    val rel = n.getSingleRelationship(IsA, OUTGOING)
    (rel != null) && (rel.getEndNode.getId == getSubrefNodeIdFor(entityType))
  }
}

private[neo4j] object SubreferenceNodeSupport {

  private final val Instances = MutableMap.empty[GraphDatabaseService, SubreferenceNodeSupport]

  def apply(db: GraphDatabaseService): SubreferenceNodeSupport = {
    if (!Instances.contains(db)) {
      Instances += db -> new SubreferenceNodeSupport(db)
    }
    Instances(db)
  }
}
