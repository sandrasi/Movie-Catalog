package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.Entity
import com.github.sandrasi.moviecatalog.repository.neo4j.transaction.TransactionSupport
import java.io.Closeable
import java.util.UUID
import org.neo4j.graphdb.{DynamicLabel, Label, GraphDatabaseService, Node}
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.tooling.GlobalGraphOperations
import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MutableMap}
import scala.reflect.runtime.universe

private[neo4j] class DatabaseManager(db: GraphDatabaseService) extends TransactionSupport {

  Validate.notNull(db)

  private final val EntityIdIndex = db.index().forNodes("Node")

  def getNodeById(id: UUID): Option[Node] = {
    val query = new BooleanQuery()
    query.add(new TermQuery(new Term(Uuid, id.toString)), MUST)
    Option(EntityIdIndex.query(query).getSingle)
  }

  def getNodeOf(e: Entity): Node = e.id.flatMap(getNodeById(_)) match {
    case Some(n) => if (isNodeOfType(n, e.getClass)) n else throw new ClassCastException(s"Node [id: ${n.getId}] is not of type ${e.getClass.getName}".format(n.getId, e.getClass.getName))
    case None => throw new NoSuchElementException(s"$e is not in the database")
  }

  def createNodeFor(e: Entity): Node = if (e.id.isEmpty) {
    val uuid = UUID.randomUUID
    val node = db.createNode()
    PropertyManager.setUuid(node, uuid)
    EntityIdIndex.add(node, Uuid, uuid.toString)
    node
  } else throw new IllegalStateException(s"Entity $e already has an id: $id")

  def getSubreferenceNode[A <: Entity](c: Class[A]): Node = db.getNodeById(getSubreferenceNodeId(c))

  def isSubreferenceNode(n: Node): Boolean = SubreferenceRelationshipType.values.exists(v => getOrCreateSubreferenceNodeId(v) == n.getId)

  private def getSubreferenceNodeId[A <: Entity](c: Class[A]): Long = try {
    getOrCreateSubreferenceNodeId(SubreferenceRelationshipType.forClass(c))
  } catch {
    case _: NoSuchElementException => throw new IllegalArgumentException(s"Unsupported entity type ${c.getName}")
  }

  private def getOrCreateSubreferenceNodeId(relType: SubreferenceRelationshipType): Long = Option(db.getReferenceNode.getSingleRelationship(relType, OUTGOING)).map(_.getEndNode).getOrElse(createSubreferenceNodeForRelationshipType(relType)).getId

  private def createSubreferenceNodeForRelationshipType(relType: SubreferenceRelationshipType) = transaction(db) {
    val srn = db.createNode()
    srn.setProperty(SubreferenceNodeClassName, relType.name)
    db.getReferenceNode.createRelationshipTo(srn, relType)
    srn
  }

  def isNodeOfType[A <: Entity](n: Node, entityType: Class[A]): Boolean = n.getRelationships(IsA, OUTGOING).asScala.view.map(_.getEndNode.getId).exists(_ == getSubreferenceNodeId(entityType))
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
