package com.github.sandrasi.moviecatalog.repository.neo4j

import java.util.Locale
import java.util.Locale.US
import org.neo4j.graphdb.{GraphDatabaseService, NotFoundException}
import com.github.sandrasi.moviecatalog.domain.entities.base.LongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.{Actor, Actress}
import com.github.sandrasi.moviecatalog.domain.entities.container._
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}
import com.github.sandrasi.moviecatalog.repository.Repository
import com.github.sandrasi.moviecatalog.repository.neo4j.transaction.TransactionSupport

class Neo4jRepository(db: GraphDatabaseService) extends Repository with TransactionSupport {

  private final val EntityFactory = com.github.sandrasi.moviecatalog.repository.neo4j.utility.EntityFactory(db)
  private final val NodeFactory = com.github.sandrasi.moviecatalog.repository.neo4j.utility.NodeFactory(db)
  private final val RelationshipFactory = com.github.sandrasi.moviecatalog.repository.neo4j.utility.RelationshipFactory(db)

  override def get[A <: LongIdEntity](id: Long, entityType: Class[A])(implicit locale: Locale = US): Option[A] = try {
    Some(EntityFactory.createEntityFrom(db.getNodeById(id), entityType))
  } catch {
    case _: NotFoundException | _: ClassCastException | _: IllegalArgumentException => try {
      Some(EntityFactory.createEntityFrom(db.getRelationshipById(id), entityType))
    } catch {
      case _: NotFoundException | _: ClassCastException => None
    }
  }

  override def save[A <: LongIdEntity](entity: A)(implicit locale: Locale = US): A = transaction(db) {
    if (entity.id == None) insert(entity, locale) else update(entity, locale)
  }

  private def insert[A <: LongIdEntity](e: A, l: Locale): A = e match {
    case _: Actor | _: Actress => EntityFactory.createEntityFrom(RelationshipFactory.createRelationshipFrom(e), e.getClass).asInstanceOf[A]
    case _: Character | _: DigitalContainer | _: Movie | _: Person | _: Soundtrack | _: Subtitle => EntityFactory.createEntityFrom(NodeFactory.createNodeFrom(e), e.getClass)(l).asInstanceOf[A]
    case _ => throw new IllegalArgumentException("Unsupported entity type: %s".format(e.getClass.getName))
  }

  private def update[A <: LongIdEntity](e: A, l: Locale): A = e match {
    case _ => throw new IllegalArgumentException("Unsupported entity type: %s".format(e.getClass.getName))
  }

  override def search(text: String)(implicit locale: Locale = US): Set[LongIdEntity] = throw new UnsupportedOperationException("Not yet implemented")
}
