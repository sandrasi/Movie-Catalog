package com.github.sandrasi.moviecatalog.repository.neo4j

import java.util.{UUID, Locale}
import java.util.Locale.US
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.kernel.EmbeddedGraphDatabase
import com.github.sandrasi.moviecatalog.domain.Entity
import com.github.sandrasi.moviecatalog.repository.{Repository, RepositoryFactory}
import com.github.sandrasi.moviecatalog.repository.neo4j.transaction.TransactionSupport
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.{EntityFactory, IndexManager, NodeManager}

class Neo4jRepository(db: GraphDatabaseService) extends Repository with TransactionSupport {

  private final val EntityFact = EntityFactory(db)
  private final val NodeMgr = NodeManager(db)
  private final val IdxMgr = IndexManager(db)
  private final val ExecutionEngine = new ExecutionEngine(db)

  override def get[A <: Entity](uuid: UUID, entityType: Class[A])(implicit locale: Locale = US): Option[A] = try {
    Some(EntityFact.createEntityFrom(NodeMgr.getNode(uuid), entityType))
  } catch {
    case _: ClassCastException | _: IllegalArgumentException | _: NoSuchElementException => None
  }

  override def save[A <: Entity](entity: A)(implicit locale: Locale = US): A = {
    implicit val tx = db.beginTx()
    transaction(tx) {
      EntityFact.createEntityFrom(if (entity.id.isEmpty) NodeMgr.createNodeFrom(entity) else NodeMgr.updateNodeOf(entity), entity.getClass).asInstanceOf[A]
    }
  }

  override def delete(entity: Entity) {
    implicit val tx = db.beginTx()
    transaction(tx) { NodeMgr.deleteNodeOf(entity) }
  }

  override def query[A <: Entity](entityType: Class[A], predicate: A => Boolean = (_: A) => true): Iterator[A] = NodeMgr.getNodesOfType(entityType).map(EntityFact.createEntityFrom(_, entityType)).filter(predicate(_))

  override def search(text: String)(implicit locale: Locale = US): Iterator[Entity] = {

    val result = ExecutionEngine.execute("START nodes=node:MotionPicture(%s) RETURN nodes".format(text))
    println(result.dumpToString())

    throw new UnsupportedOperationException("Not yet implemented")
  }

  def shutdown() {
    db.shutdown()
  }
}

object Neo4jRepository extends RepositoryFactory {

  def apply(repositoryConfiguration: RepositoryConfiguration) = new Neo4jRepository(new EmbeddedGraphDatabase(repositoryConfiguration.get("storeDir", classOf[String])))

  val configurationMetaData = ConfigurationMetaData(
    ConfigurationParameterMetaData(
      name = "storeDir",
      description = "The directory where Neo4j stores the database",
      valueType = classOf[String],
      parameterConverter = convertStringsToString
    )
  )

  private def convertStringsToString(strs: Seq[String]): ParameterConversionResult[String] = if (strs.nonEmpty) Right(strs.head) else Left(new IllegalArgumentException("Cannot convert strings to string"))
}
