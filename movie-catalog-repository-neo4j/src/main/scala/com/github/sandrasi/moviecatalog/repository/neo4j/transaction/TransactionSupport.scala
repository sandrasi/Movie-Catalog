package com.github.sandrasi.moviecatalog.repository.neo4j.transaction

import org.neo4j.graphdb.{GraphDatabaseService, Transaction}

trait TransactionSupport {

  protected def transaction[A <: Any](db: GraphDatabaseService)(dbOp: => A): A = transaction(db.beginTx())(dbOp)

  protected def transaction[A <: Any](tx: Transaction)(dbOp: => A): A = {
    try {
      val result = dbOp
      tx.success()
      result
    } finally {
      tx.finish()
    }
  }
}
