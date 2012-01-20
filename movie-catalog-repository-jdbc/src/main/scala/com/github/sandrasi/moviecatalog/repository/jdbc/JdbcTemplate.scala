package com.github.sandrasi.moviecatalog.repository.jdbc

import java.sql.Connection
import javax.sql.DataSource

class JdbcTemplate(val ds: DataSource) {

  def queryToList[A](sql: String, rowMapper: RowMapper[A], args: Any*): List[A] = {
    query(sql, rowMapper, args: _*).toList
  }

  def query[A](sql: String, rowMapper: RowMapper[A], args: Any*): Iterator[A] = {
    val conn = ds.getConnection
    val ps = createStatementWithArguments(conn, sql, args: _*)
    val rs = ps.executeQuery
    new Iterator[A] {

      jumpToFirstRecord()

      override def hasNext = {
        !conn.isClosed && !rs.isAfterLast
      }

      override def next() = {
        if (hasNext) {
          val result = rowMapper.map(rs)
          if (!rs.next) conn.close()
          result
        } else throw new NoSuchElementException("No more records")
      }

      private def jumpToFirstRecord() = rs.next
    }
  }

  def update(sql: String, args: Any*): Int = update(sql, new GeneratedKeyHolder(), args: _*)

  def update(sql: String, keyHolder: GeneratedKeyHolder, args: Any*): Int = {
    val conn = ds.getConnection
    val ps = createStatementWithArguments(conn, sql, args: _*)
    val result = ps.executeUpdate()
    keyHolder.keyBuffer += 1l
    conn.close()
    result
  }

  private def createStatementWithArguments(conn: Connection, sql: String, args: Any*) = {
    val ps = conn.prepareStatement(sql)
    for (i <- 0 until args.length) ps.setObject(i + 1, args(i))
    ps
  }
}
