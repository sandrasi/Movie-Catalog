package com.github.sandrasi.moviecatalog.repository.neo4j.transaction

import scala.collection.JavaConverters._
import org.junit.runner.RunWith
import org.neo4j.graphdb.NotInTransactionException
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport

@RunWith(classOf[JUnitRunner])
class TransactionSupportTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with ShouldMatchers with MovieCatalogNeo4jSupport {

  test("should execute operation in transaction") {
    try {
      val node = transaction(db) { db.createNode() }
      db.getNodeById(node.getId) should be(node)
    } catch {
      case _: NotInTransactionException => fail("db.createNode() should not have thrown an exception")
    }
  }
  
  test("should abort transaction if an exception occurs") {
    try {
      getNodeCount should be(1)
      transaction(db) {
        db.createNode()
        throw new Exception("test exception")
      }
      fail("transaction(GraphDatabaseService) should have thrown an exception")
    } catch {
      case _: Exception => getNodeCount should be(1)
    }
  }
}
