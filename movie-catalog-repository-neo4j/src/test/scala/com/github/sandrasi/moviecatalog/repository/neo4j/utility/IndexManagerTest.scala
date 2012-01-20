package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport

@RunWith(classOf[JUnitRunner])
class IndexManagerTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with ShouldMatchers with MovieCatalogNeo4jSupport {

  private var subject: IndexManager = _

  override protected def beforeEach() {
    subject = IndexManager(db)
  }

  test("should return the same index manager instance for the same database") {
    subject should be theSameInstanceAs(IndexManager(db))
  }

  test("should return different index manager instances for different databases") {
    subject should not be theSameInstanceAs(IndexManager(createTempDb()))
  }

  test("should not instantiate index manager if the database is null") {
    intercept[IllegalArgumentException] {
      IndexManager(null)
    }
  }
  
//  test("should not index the node representing an unsupported type") {
//    intercept[IllegalArgumentException] {
//      val node = createNode()
//      subject.index(node, classOf[AnyRef])
//    }
//  }
}
