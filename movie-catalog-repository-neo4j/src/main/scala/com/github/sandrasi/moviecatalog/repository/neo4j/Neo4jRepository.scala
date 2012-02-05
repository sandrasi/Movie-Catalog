package com.github.sandrasi.moviecatalog.repository.neo4j

import java.util.Locale
import java.util.Locale.US
import org.neo4j.graphdb.{GraphDatabaseService, NotFoundException}
import com.github.sandrasi.moviecatalog.domain.entities.base.LongIdEntity
import com.github.sandrasi.moviecatalog.repository.Repository
import com.github.sandrasi.moviecatalog.repository.neo4j.transaction.TransactionSupport

class Neo4jRepository(db: GraphDatabaseService) extends Repository with TransactionSupport {

  private final val EntityFactory = com.github.sandrasi.moviecatalog.repository.neo4j.utility.EntityFactory(db)
  private final val NodeManager = com.github.sandrasi.moviecatalog.repository.neo4j.utility.NodeManager(db)

  override def get[A <: LongIdEntity](id: Long, entityType: Class[A])(implicit locale: Locale = US): Option[A] = try {
    Some(EntityFactory.createEntityFrom(db.getNodeById(id), entityType))
  } catch {
    case _: NotFoundException | _: ClassCastException | _: IllegalArgumentException => None
  }

  override def save[A <: LongIdEntity](entity: A)(implicit locale: Locale = US): A = transaction(db) {
    EntityFactory.createEntityFrom(if (entity.id == None) NodeManager.createNodeFrom(entity)(locale) else NodeManager.updateNodeOf(entity)(locale), entity.getClass)(locale).asInstanceOf[A]
  }

  override def search(text: String)(implicit locale: Locale = US): Set[LongIdEntity] = throw new UnsupportedOperationException("Not yet implemented")

  override def delete[A <: LongIdEntity](id: Long, entityType: Class[A]) { throw new UnsupportedOperationException("Not yet implemented") }
}
