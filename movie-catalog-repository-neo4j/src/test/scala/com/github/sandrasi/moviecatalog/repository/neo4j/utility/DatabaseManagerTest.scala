package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.EntityRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport
import java.util.UUID

@RunWith(classOf[JUnitRunner])
class DatabaseManagerTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with Matchers with MovieCatalogNeo4jSupport {

  private var subject: DatabaseManager = _

  override protected def beforeEach() {
    subject = DatabaseManager(db)
  }

  test("should return the same node manager instance for the same database") {
    subject should be theSameInstanceAs(DatabaseManager(db))
  }

  test("should return different node manager instances for different databases") {
    subject should not be theSameInstanceAs(DatabaseManager(createTempDb()))
  }

  test("should not instantiate the node manager if the database is null") {
    intercept[IllegalArgumentException] {
      DatabaseManager(null)
    }
  }

  test("should return the node representing the entity") {
    val node = createNodeFrom(VincentVega)
    val persistedEntity = createCharacterFrom(node)
    subject.getNodeOf(persistedEntity) should be(node)
  }

  test("should not return anything if the entity is not yet persisted") {
    intercept[NoSuchElementException] {
      subject.getNodeOf(VincentVega)
    }
  }

  test("should not return anything if no node with given id is found in the database") {
    intercept[NoSuchElementException] {
      subject.getNodeOf(Actor(JohnTravolta, VincentVega, PulpFiction, id = UUID.randomUUID))
    }
  }

  test("should create node from entity without id") {
    val node = transaction(db) { subject.createNodeFor(JohnTravolta) }
    node should not be(null)
  }

  test("should not create node from entity with id") {
    intercept[IllegalStateException] {
      subject.createNodeFor(Actor(JohnTravolta, VincentVega, PulpFiction, id = UUID.randomUUID))
    }
  }

  test("should create subreference nodes if they don't exist") {
    getNodeCount should be(1)
    subject.getSubreferenceNode(classOf[Cast])
    getNodeCount should be(2)
    subject.getSubreferenceNode(classOf[Actor])
    getNodeCount should be(3)
    subject.getSubreferenceNode(classOf[Actress])
    getNodeCount should be(4)
    subject.getSubreferenceNode(classOf[Character])
    getNodeCount should be(5)
    subject.getSubreferenceNode(classOf[Genre])
    getNodeCount should be(6)
    subject.getSubreferenceNode(classOf[DigitalContainer])
    getNodeCount should be(7)
    subject.getSubreferenceNode(classOf[Movie])
    getNodeCount should be(8)
    subject.getSubreferenceNode(classOf[Person])
    getNodeCount should be(9)
    subject.getSubreferenceNode(classOf[Soundtrack])
    getNodeCount should be(10)
    subject.getSubreferenceNode(classOf[Subtitle])
    getNodeCount should be(11)
  }

  test("should reuse subreference nodes if they already exist") {
    getNodeCount should be(1)
    subject.getSubreferenceNode(classOf[Cast])
    getNodeCount should be(2)
    subject.getSubreferenceNode(classOf[Cast])
    getNodeCount should be(2)
  }

  test("should not return a subreference node id for unsupported types") {
    intercept[IllegalArgumentException] {
      subject.getSubreferenceNode(classOf[Entity])
    }
  }

  test("should return true for subreference nodes") {
    val abstractCastSubrefNode = subject.getSubreferenceNode(classOf[Cast])
    val actorSubrefNode = subject.getSubreferenceNode(classOf[Actor])
    val actressSubrefNode = subject.getSubreferenceNode(classOf[Actress])
    val characterSubrefNode = subject.getSubreferenceNode(classOf[Character])
    val genreSubrefNode = subject.getSubreferenceNode(classOf[Genre])
    val digitalContainerSubrefNode = subject.getSubreferenceNode(classOf[DigitalContainer])
    val movieSubrefNode = subject.getSubreferenceNode(classOf[Movie])
    val personSubrefNode = subject.getSubreferenceNode(classOf[Person])
    val soundtrackSubrefNode = subject.getSubreferenceNode(classOf[Soundtrack])
    val subtitleSubrefNode = subject.getSubreferenceNode(classOf[Subtitle])
    assert(subject.isSubreferenceNode(abstractCastSubrefNode))
    assert(subject.isSubreferenceNode(actorSubrefNode))
    assert(subject.isSubreferenceNode(actressSubrefNode))
    assert(subject.isSubreferenceNode(characterSubrefNode))
    assert(subject.isSubreferenceNode(genreSubrefNode))
    assert(subject.isSubreferenceNode(digitalContainerSubrefNode))
    assert(subject.isSubreferenceNode(movieSubrefNode))
    assert(subject.isSubreferenceNode(personSubrefNode))
    assert(subject.isSubreferenceNode(soundtrackSubrefNode))
    assert(subject.isSubreferenceNode(subtitleSubrefNode))
  }

  test("should return false for non-subreference nodes") {
    assert(!subject.isSubreferenceNode(createNode()))
  }

  test("should return true if the node is connected to the expected subreference node") {
    val n = createNode()
    transaction(db) { n.createRelationshipTo(subject.getSubreferenceNode(classOf[Cast]), IsA) }
    assert(subject.isNodeOfType(n, classOf[Cast]))
  }

  test("should return false if the node is not connected to the expected subreference node") {
    val n = createNode()
    transaction(db) { n.createRelationshipTo(subject.getSubreferenceNode(classOf[Cast]), IsA) }
    assert(!subject.isNodeOfType(n, classOf[Actor]))
  }

  test("should not check if the node is connected to the expected subreference node for unsupported types") {
    val n = createNode()
    transaction(db) { n.createRelationshipTo(subject.getSubreferenceNode(classOf[Cast]), IsA) }
    intercept[IllegalArgumentException] {
      subject.isNodeOfType(n, classOf[Entity])
    }
  }
}
