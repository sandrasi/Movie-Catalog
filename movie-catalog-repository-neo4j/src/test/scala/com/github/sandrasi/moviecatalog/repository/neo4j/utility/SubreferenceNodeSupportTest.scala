package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.JavaConversions._
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.{AbstractCast, Actor, Actress}
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}
import com.github.sandrasi.moviecatalog.domain.entities.container.{DigitalContainer, Soundtrack, Subtitle}
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.EntityRelationshipType.IsA
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport

@RunWith(classOf[JUnitRunner])
class SubreferenceNodeSupportTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with ShouldMatchers with MovieCatalogNeo4jSupport {

  private var subject: SubreferenceNodeSupport = _

  override protected def beforeEach() {
    subject = SubreferenceNodeSupport(db)
  }

  test("should return the same subreference node support instance for the same database") {
    subject should be theSameInstanceAs(SubreferenceNodeSupport(db))
  }

  test("should return different subreference node support instances for different databases") {
    subject should not be theSameInstanceAs(SubreferenceNodeSupport(createTempDb()))
  }

  test("should not instantiate entity subreference node support if the database is null") {
    intercept[IllegalArgumentException] {
      SubreferenceNodeSupport(null)
    }
  }

  test("should create subreference nodes if they don't exist") {
    iterableAsScalaIterable(db.getAllNodes) should have size(1)
    subject.getSubrefNodeIdFor(classOf[AbstractCast])
    iterableAsScalaIterable(db.getAllNodes) should have size(2)
    subject.getSubrefNodeIdFor(classOf[Character])
    iterableAsScalaIterable(db.getAllNodes) should have size(3)
    subject.getSubrefNodeIdFor(classOf[DigitalContainer])
    iterableAsScalaIterable(db.getAllNodes) should have size(4)
    subject.getSubrefNodeIdFor(classOf[Movie])
    iterableAsScalaIterable(db.getAllNodes) should have size(5)
    subject.getSubrefNodeIdFor(classOf[Person])
    iterableAsScalaIterable(db.getAllNodes) should have size(6)
    subject.getSubrefNodeIdFor(classOf[Soundtrack])
    iterableAsScalaIterable(db.getAllNodes) should have size(7)
    subject.getSubrefNodeIdFor(classOf[Subtitle])
    iterableAsScalaIterable(db.getAllNodes) should have size(8)
  }

  test("should reuse subreference nodes if they already exist") {
    iterableAsScalaIterable(db.getAllNodes) should have size(1)
    subject.getSubrefNodeIdFor(classOf[AbstractCast])
    iterableAsScalaIterable(db.getAllNodes) should have size(2)
    subject.getSubrefNodeIdFor(classOf[AbstractCast])
    iterableAsScalaIterable(db.getAllNodes) should have size(2)
  }

  test("should return true for subreference nodes") {
    val actorSubrefNode = db.getNodeById(subject.getSubrefNodeIdFor(classOf[Actor]))
    val actressSubrefNode = db.getNodeById(subject.getSubrefNodeIdFor(classOf[Actress]))
    val abstractCastSubrefNode = db.getNodeById(subject.getSubrefNodeIdFor(classOf[AbstractCast]))
    val characterSubrefNode = db.getNodeById(subject.getSubrefNodeIdFor(classOf[Character]))
    val digitalContainerSubrefNode = db.getNodeById(subject.getSubrefNodeIdFor(classOf[DigitalContainer]))
    val movieSubrefNode = db.getNodeById(subject.getSubrefNodeIdFor(classOf[Movie]))
    val personSubrefNode = db.getNodeById(subject.getSubrefNodeIdFor(classOf[Person]))
    val soundtrackSubrefNode = db.getNodeById(subject.getSubrefNodeIdFor(classOf[Soundtrack]))
    val subtitleSubrefNode = db.getNodeById(subject.getSubrefNodeIdFor(classOf[Subtitle]))
    subject.isSubreferenceNode(abstractCastSubrefNode) should be(true)
    subject.isSubreferenceNode(actorSubrefNode) should be(true)
    subject.isSubreferenceNode(actressSubrefNode) should be(true)
    subject.isSubreferenceNode(characterSubrefNode) should be(true)
    subject.isSubreferenceNode(digitalContainerSubrefNode) should be(true)
    subject.isSubreferenceNode(movieSubrefNode) should be(true)
    subject.isSubreferenceNode(personSubrefNode) should be(true)
    subject.isSubreferenceNode(soundtrackSubrefNode) should be(true)
    subject.isSubreferenceNode(subtitleSubrefNode) should be(true)
  }

  test("should return false for non-subreference nodes") {
    subject.isSubreferenceNode(createNode()) should be(false)
  }

  test("should return true if the node is connected to the expected subreference node") {
    val n = createNode()
    transaction(db) { n.createRelationshipTo(db.getNodeById(subject.getSubrefNodeIdFor(classOf[Character])), IsA) }
    subject.isNodeOfType(n, classOf[Character]) should be(true)
  }

  test("should return false if the node is not connected to the expected subreference node") {
    val n = createNode()
    transaction(db) { n.createRelationshipTo(db.getNodeById(subject.getSubrefNodeIdFor(classOf[DigitalContainer])), IsA) }
    subject.isNodeOfType(n, classOf[Character]) should be(false)
  }

  test("should not check if the node is connected to the expected subreference node for unsupported types") {
    val n = createNode()
    transaction(db) { n.createRelationshipTo(db.getNodeById(subject.getSubrefNodeIdFor(classOf[Character])), IsA) }
    intercept[IllegalArgumentException] {
      subject.isNodeOfType(n, classOf[VersionedLongIdEntity])
    }
  }
}
