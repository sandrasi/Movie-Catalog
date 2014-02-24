package com.github.sandrasi.moviecatalog.repository.neo4j.transaction

import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport
import org.junit.runner.RunWith
import org.neo4j.graphdb.NotInTransactionException
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TransactionSupportTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with Matchers with MovieCatalogNeo4jSupport {

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
