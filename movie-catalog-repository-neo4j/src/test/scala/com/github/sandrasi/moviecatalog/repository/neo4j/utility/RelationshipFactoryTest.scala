package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import org.neo4j.graphdb.Direction.OUTGOING
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.base.LongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.{Actor, Actress}
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.CharacterRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.FilmCrewRelationshipType
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport

class RelationshipFactoryTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with ShouldMatchers with MovieCatalogNeo4jSupport {

  private var subject: RelationshipFactory = _

  override protected def beforeEach() {
    subject = RelationshipFactory(db)
  }

  test("should return the same relationship factory instance for the same database") {
    subject should be theSameInstanceAs(RelationshipFactory(db))
  }

  test("should return different relationship factory instances for different databases") {
    subject should not be theSameInstanceAs(RelationshipFactory(createTempDb()))
  }

  test("should not instantiate relationship factory if the database is null") {
    intercept[IllegalArgumentException] {
      RelationshipFactory(null)
    }
  }

  test("should create relationship from actor") {
    val personNode = createNode(JohnDoe)
    val characterNode = createNode(Johnny)
    val movieNode = createNode(TestMovie)
    val actorRelationship = transaction(db) { subject.createRelationshipFrom(Actor(createPersonEntity(personNode), createCharacterEntity(characterNode), createMovieEntity(movieNode))) }
    actorRelationship.getType should be(FilmCrewRelationshipType.Actor)
    actorRelationship.getStartNode should be(personNode)
    actorRelationship.getEndNode should  be(movieNode)
    val playedByRelationship = characterNode.getSingleRelationship(PlayedBy, OUTGOING)
    playedByRelationship.getStartNode should be(characterNode)
    playedByRelationship.getEndNode should be(personNode)
    playedByRelationship.getProperty("castRelationshipId") should be(actorRelationship.getId)
    val appearedInRelationship = characterNode.getSingleRelationship(AppearedIn, OUTGOING)
    appearedInRelationship.getStartNode should be(characterNode)
    appearedInRelationship.getEndNode should be(movieNode)
    appearedInRelationship.getProperty("castRelationshipId") should be(actorRelationship.getId)
  }

  test("should create relationship from actress") {
    val personNode = createNode(JaneDoe)
    val characterNode = createNode(Jenny)
    val movieNode = createNode(TestMovie)
    val actressRelationship = transaction(db) { subject.createRelationshipFrom(Actress(createPersonEntity(personNode), createCharacterEntity(characterNode), createMovieEntity(movieNode))) }
    actressRelationship.getType should be(FilmCrewRelationshipType.Actress)
    actressRelationship.getStartNode should be(personNode)
    actressRelationship.getEndNode should  be(movieNode)
    val playedByRelationship = characterNode.getSingleRelationship(PlayedBy, OUTGOING)
    playedByRelationship.getStartNode should be(characterNode)
    playedByRelationship.getEndNode should be(personNode)
    playedByRelationship.getProperty("castRelationshipId") should be(actressRelationship.getId)
    val appearedInRelationship = characterNode.getSingleRelationship(AppearedIn, OUTGOING)
    appearedInRelationship.getStartNode should be(characterNode)
    appearedInRelationship.getEndNode should be(movieNode)
    appearedInRelationship.getProperty("castRelationshipId") should be(actressRelationship.getId)
  }

  test("should not create relationship from the actor if the person does not exist in the database") {
    intercept[IllegalStateException] {
      subject.createRelationshipFrom(Actor(JohnDoe, saveEntity(Johnny), saveEntity(TestMovie)))
    }
  }

  test("should not create relationship from the actor if the character does not exist in the database") {
    intercept[IllegalStateException] {
      subject.createRelationshipFrom(Actor(saveEntity(JohnDoe), Johnny, saveEntity(TestMovie)))
    }
  }

  test("should not create relationship from the actor if the movie does not exist in the database") {
    intercept[IllegalStateException] {
      subject.createRelationshipFrom(Actor(saveEntity(JohnDoe), saveEntity(Johnny), TestMovie))
    }
  }

  test("should not create relationship from the actor if the actor already has an id") {
    intercept[IllegalStateException] {
      subject.createRelationshipFrom(Actor(saveEntity(JohnDoe), saveEntity(Johnny), saveEntity(TestMovie), 1))
    }
  }

  test("should not create node from unsupported entity") {
    intercept[IllegalArgumentException] {
      subject.createRelationshipFrom(new LongIdEntity(0) {})
    }
  }
}
