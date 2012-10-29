package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.JavaConverters._
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
@deprecated class SubreferenceNodeSupportTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with ShouldMatchers with MovieCatalogNeo4jSupport {

  private var subject: SubreferenceNodeSupport = _

  override protected def beforeEach() {
    subject = SubreferenceNodeSupport(db)
  }

  ignore("should return the same subreference node support instance for the same database") {
    subject should be theSameInstanceAs(SubreferenceNodeSupport(db))
  }

  ignore("should return different subreference node support instances for different databases") {
    subject should not be theSameInstanceAs(SubreferenceNodeSupport(createTempDb()))
  }

  ignore("should not instantiate entity subreference node support if the database is null") {
    intercept[IllegalArgumentException] {
      SubreferenceNodeSupport(null)
    }
  }

  ignore("should create subreference nodes if they don't exist") {
    getNodeCount should be(1)
    subject.getSubrefNodeIdFor(classOf[AbstractCast])
    getNodeCount should be(2)
    subject.getSubrefNodeIdFor(classOf[Actor])
    getNodeCount should be(3)
    subject.getSubrefNodeIdFor(classOf[Actress])
    getNodeCount should be(4)
    subject.getSubrefNodeIdFor(classOf[Character])
    getNodeCount should be(5)
    subject.getSubrefNodeIdFor(classOf[DigitalContainer])
    getNodeCount should be(6)
    subject.getSubrefNodeIdFor(classOf[Movie])
    getNodeCount should be(7)
    subject.getSubrefNodeIdFor(classOf[Person])
    getNodeCount should be(8)
    subject.getSubrefNodeIdFor(classOf[Soundtrack])
    getNodeCount should be(9)
    subject.getSubrefNodeIdFor(classOf[Subtitle])
    getNodeCount should be(10)
  }

  ignore("should reuse subreference nodes if they already exist") {
    getNodeCount should be(1)
    subject.getSubrefNodeIdFor(classOf[AbstractCast])
    getNodeCount should be(2)
    subject.getSubrefNodeIdFor(classOf[AbstractCast])
    getNodeCount should be(2)
  }

  ignore("should not return a subreference node id for unsupported types") {
    intercept[IllegalArgumentException] {
      subject.getSubrefNodeIdFor(classOf[VersionedLongIdEntity])
    }
  }

  ignore("should return true for subreference nodes") {
    val abstractCastSubrefNode = db.getNodeById(subject.getSubrefNodeIdFor(classOf[AbstractCast]))
    val actorSubrefNode = db.getNodeById(subject.getSubrefNodeIdFor(classOf[Actor]))
    val actressSubrefNode = db.getNodeById(subject.getSubrefNodeIdFor(classOf[Actress]))
    val characterSubrefNode = db.getNodeById(subject.getSubrefNodeIdFor(classOf[Character]))
    val digitalContainerSubrefNode = db.getNodeById(subject.getSubrefNodeIdFor(classOf[DigitalContainer]))
    val movieSubrefNode = db.getNodeById(subject.getSubrefNodeIdFor(classOf[Movie]))
    val personSubrefNode = db.getNodeById(subject.getSubrefNodeIdFor(classOf[Person]))
    val soundtrackSubrefNode = db.getNodeById(subject.getSubrefNodeIdFor(classOf[Soundtrack]))
    val subtitleSubrefNode = db.getNodeById(subject.getSubrefNodeIdFor(classOf[Subtitle]))
    assert(subject.isSubreferenceNode(abstractCastSubrefNode))
    assert(subject.isSubreferenceNode(actorSubrefNode))
    assert(subject.isSubreferenceNode(actressSubrefNode))
    assert(subject.isSubreferenceNode(characterSubrefNode))
    assert(subject.isSubreferenceNode(digitalContainerSubrefNode))
    assert(subject.isSubreferenceNode(movieSubrefNode))
    assert(subject.isSubreferenceNode(personSubrefNode))
    assert(subject.isSubreferenceNode(soundtrackSubrefNode))
    assert(subject.isSubreferenceNode(subtitleSubrefNode))
  }

  ignore("should return false for non-subreference nodes") {
    assert(!subject.isSubreferenceNode(createNode()))
  }

  ignore("should return true if the node is connected to the expected subreference node") {
    val n = createNode()
    transaction(db) { n.createRelationshipTo(db.getNodeById(subject.getSubrefNodeIdFor(classOf[AbstractCast])), IsA) }
    assert(subject.isNodeOfType(n, classOf[AbstractCast]))
  }

  ignore("should return false if the node is not connected to the expected subreference node") {
    val n = createNode()
    transaction(db) { n.createRelationshipTo(db.getNodeById(subject.getSubrefNodeIdFor(classOf[AbstractCast])), IsA) }
    assert(!subject.isNodeOfType(n, classOf[Actor]))
  }

  ignore("should not check if the node is connected to the expected subreference node for unsupported types") {
    val n = createNode()
    transaction(db) { n.createRelationshipTo(db.getNodeById(subject.getSubrefNodeIdFor(classOf[AbstractCast])), IsA) }
    intercept[IllegalArgumentException] {
      subject.isNodeOfType(n, classOf[VersionedLongIdEntity])
    }
  }
}
