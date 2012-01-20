package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.mutable.{Map => MutableMap}
import java.lang.IllegalStateException
import org.neo4j.graphdb.{GraphDatabaseService, Relationship}
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.CharacterRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.FilmCrewRelationshipType
import com.github.sandrasi.moviecatalog.domain.entities.base.LongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.AbstractCast

private[neo4j] class RelationshipFactory private (db: GraphDatabaseService) extends MovieCatalogGraphPropertyNames {
  
  Validate.notNull(db)

  def createRelationshipFrom[A <: LongIdEntity](e: A): Relationship = e match {
    case ac: AbstractCast => createRelationshipFrom(ac)
    case _ => throw new IllegalArgumentException("Unsupported entity type: %s".format(e.getClass.getName))
  }

  private def createRelationshipFrom(ac: AbstractCast): Relationship = {
    if (ac.id != None) throw new IllegalStateException("Entity %s already has an id: %d".format(ac.getClass.getName, ac.id.get))
    val personNode = getNodeById(ac.person)
    val characterNode = getNodeById(ac.character)
    val motionPictureNode = getNodeById(ac.motionPicture)
    val castRelationship = personNode.createRelationshipTo(motionPictureNode, FilmCrewRelationshipType.forClass(ac.getClass))
    val playedByRelationship = characterNode.createRelationshipTo(personNode, PlayedBy)
    playedByRelationship.setProperty(PlayedByRelationshipCastRelationshipId, castRelationship.getId)
    val appearedInRelationship = characterNode.createRelationshipTo(motionPictureNode, AppearedIn)
    appearedInRelationship.setProperty(AppearedInRelationshipCastRelationshipId, castRelationship.getId)
    castRelationship
  }

  private def getNodeById[A <: LongIdEntity](e: A) = if (e.id != None) db.getNodeById(e.id.get) else throw new IllegalStateException("%s is not in the database".format(e))
}

private[neo4j] object RelationshipFactory {

  private final val Instances = MutableMap.empty[GraphDatabaseService, RelationshipFactory]

  def apply(db: GraphDatabaseService): RelationshipFactory = {
    if (!Instances.contains(db)) {
      Instances += db -> new RelationshipFactory(db)
    }
    Instances(db)
  }
}
