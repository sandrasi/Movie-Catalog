package com.github.sandrasi.moviecatalog.repository.neo4j

import java.util.Locale
import java.util.Locale.US
import org.neo4j.graphdb.{GraphDatabaseService, NotFoundException}
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.repository.Repository
import com.github.sandrasi.moviecatalog.repository.neo4j.transaction.TransactionSupport

class Neo4jRepository(db: GraphDatabaseService) extends Repository with TransactionSupport {

  private final val EntityFactory = com.github.sandrasi.moviecatalog.repository.neo4j.utility.EntityFactory(db)
  private final val NodeManager = com.github.sandrasi.moviecatalog.repository.neo4j.utility.NodeManager(db)

  override def get[A <: VersionedLongIdEntity](id: Long, entityType: Class[A])(implicit locale: Locale = US): Option[A] = try {
    Some(EntityFactory.createEntityFrom(db.getNodeById(id), entityType))
  } catch {
    case _: NotFoundException | _: ClassCastException | _: IllegalArgumentException => None
  }

  override def save[A <: VersionedLongIdEntity](entity: A)(implicit locale: Locale = US): A = transaction(db) {
    EntityFactory.createEntityFrom(if (entity.id == None) NodeManager.createNodeFrom(entity) else NodeManager.updateNodeOf(entity), entity.getClass).asInstanceOf[A]
  }

  override def delete(entity: VersionedLongIdEntity) { transaction(db) { NodeManager.deleteNodeOf(entity) } }

  override def query[A <: VersionedLongIdEntity](entityType: Class[A], predicate: A => Boolean = (_: A) => true): Traversable[A] = NodeManager.getNodesOfType(entityType).view.map(EntityFactory.createEntityFrom(_, entityType)).filter(predicate(_))

  override def search(text: String)(implicit locale: Locale = US): Traversable[VersionedLongIdEntity] = throw new UnsupportedOperationException("Not yet implemented")
}
