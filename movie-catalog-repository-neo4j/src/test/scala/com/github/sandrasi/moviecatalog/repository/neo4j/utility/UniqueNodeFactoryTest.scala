package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import org.joda.time.LocalDate
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.Actor
import com.github.sandrasi.moviecatalog.domain.entities.core.{Movie, Character, Person}
import com.github.sandrasi.moviecatalog.domain.utility.Gender._
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport

class UniqueNodeFactoryTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with ShouldMatchers with MovieCatalogNeo4jSupport {


  private var subject: UniqueNodeFactory = _

  override protected def beforeEach() {
    subject = UniqueNodeFactory(db)
  }
  
  test("should not create node from actor it a node already exists") {
    val actor = Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie))
    implicit val tx = db.beginTx()
    transaction(tx) {
      subject.createNodeFrom(actor)
    }

    intercept[IllegalArgumentException] {
      implicit val tx = db.beginTx()
      transaction(tx) {
        subject.createNodeFrom(actor)
      }
    }
  }
  
  test("should create node from actor if a different person played the same character in the same movie") {
    val actor = Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie))
    val anotherActor = Actor(insertEntity(Person("James Doe", Male, new LocalDate(1970, 7, 7), "Anytown")), insertEntity(Johnny), insertEntity(TestMovie))
    implicit val tx = db.beginTx()
    transaction(tx) {
      val actorNode = subject.createNodeFrom(actor)
      val anotherActorNode = subject.createNodeFrom(anotherActor)
      actorNode.getId should not equal(anotherActorNode.getId)
    }
  }

  test("should create node from actor if a the same person played a different character in the same movie") {
    val actor = Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie))
    val anotherActor = Actor(insertEntity(JohnDoe), insertEntity(Character("Jamie")), insertEntity(TestMovie))
    implicit val tx = db.beginTx()
    transaction(tx) {
      val actorNode = subject.createNodeFrom(actor)
      val anotherActorNode = subject.createNodeFrom(anotherActor)
      actorNode.getId should not equal(anotherActorNode.getId)
    }
  }

  test("should create node from actor if a the same person played the same character in a different movie") {
    val actor = Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie))
    val anotherActor = Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(Movie("Foo movie title")))
    implicit val tx = db.beginTx()
    transaction(tx) {
      val actorNode = subject.createNodeFrom(actor)
      val anotherActorNode = subject.createNodeFrom(anotherActor)
      actorNode.getId should not equal(anotherActorNode.getId)
    }
  }

  test("should not create node from character if a node already exists") {
    implicit val tx = db.beginTx()
    transaction(tx) {
      subject.createNodeFrom(Johnny)
    }

    intercept[IllegalArgumentException] {
      implicit val tx = db.beginTx()
      transaction(tx) {
        subject.createNodeFrom(Johnny)
      }
    }
  }
}
