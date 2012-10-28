package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport

class AbstractRelationshipTypeTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with MovieCatalogNeo4jSupport {

  test("should return true if the relationship's type matches the expected relationship type") {
    val rt = new TestRelationshipType("test")
    val r = createRelationship(createNode(), createNode(), rt)
    assert(AbstractRelationshipType.isRelationshipOfType(r, rt))
  }

  test("should return false if the relationship's type does not match the expected relationship type") {
    val r = createRelationship(createNode(), createNode(), new TestRelationshipType("test"))
    assert(!AbstractRelationshipType.isRelationshipOfType(r, new TestRelationshipType("something else")))
  }
}
