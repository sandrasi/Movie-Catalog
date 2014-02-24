package com.github.sandrasi.moviecatalog.repository.jdbc

import java.sql.{SQLException, ResultSet}
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class JdbcTemplateTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with Matchers {

  private var ds: DataSource = _
  private var subject: JdbcTemplate = _

  override protected def beforeAll() {
    ds = newDataSource("jdbc:h2:mem:jdbcTemplateTest;MODE=PostgreSQL")
  }

  override protected def beforeEach() {
    executeSql("CREATE TABLE test_table(text_field varchar, number_field int)")
    subject = new JdbcTemplate(ds)
  }

  override protected def afterEach() {
    executeSql("DROP ALL OBJECTS")
  }

  test("should iterate on query result") {
    executeSql("INSERT INTO test_table (text_field, number_field) VALUES ('test', 1)")

    val it = subject.query("SELECT text_field, number_field FROM test_table", new TestRowMapper)
    assert(it.hasNext)
    it.next should be(("test", 1))
    assert(!it.hasNext)
  }

  test("query iterator's hasNext() should return true no matter how many times it is called until next() is called") {
    executeSql("INSERT INTO test_table (text_field, number_field) VALUES ('test', 1)")

    val it = subject.query("SELECT text_field, number_field FROM test_table", new TestRowMapper)
    for (i <- 0 until new scala.util.Random().nextInt(100)) assert(it.hasNext)
    it.next()
    assert(!it.hasNext)
  }

  test("quert iterator's hasNext() should return false no matter how many times it is called after the last record is returned") {
    executeSql("INSERT INTO test_table (text_field, number_field) VALUES ('test', 1)")

    val it = subject.query("SELECT text_field, number_field FROM test_table", new TestRowMapper)
    it.next()
    for (i <- 0 until new scala.util.Random().nextInt(100)) assert(!it.hasNext)
  }

  test("query iterator's next() should throw a NoSuchElementException exception if it is called after the last result is returned") {
    executeSql("INSERT INTO test_table (text_field, number_field) VALUES ('test', 1)")

    val it = subject.query("SELECT text_field, number_field FROM test_table", new TestRowMapper)
    it.next()
    intercept[NoSuchElementException] {
      it.next()
    }
  }

  test("should retrieve query result as list") {
    executeSql("INSERT INTO test_table (text_field, number_field) VALUES ('test', 1)")

    val result = subject.queryToList("SELECT text_field, number_field FROM test_table", new TestRowMapper)
    result should be(List(("test", 1)))
  }

  test("should use the query parameters") {
    executeSql("INSERT INTO test_table (text_field, number_field) VALUES ('test_one', 1)")
    executeSql("INSERT INTO test_table (text_field, number_field) VALUES ('test_two', 2)")
    executeSql("INSERT INTO test_table (text_field, number_field) VALUES ('test_three', 3)")

    val result = subject.queryToList("SELECT text_field, number_field FROM test_table WHERE text_field = ? OR number_field = ? ORDER BY number_field",
                                     new TestRowMapper,
                                     "test_one", 2)
    result should be(List(("test_one", 1), ("test_two", 2)))
  }

  test("should not execute query with incorrect number of arguments") {
    intercept[SQLException] {
      subject.query("SELECT * FROM test_table WHERE text_field = ?", new TestRowMapper)
    }
  }

  test("should execute update and return the number of affected rows") {
    val result = subject.update("INSERT INTO test_table (text_field, number_field) VALUES ('test', 1)")
    result should be(1)
  }

  test("should use the update parameters") {
    executeSql("INSERT INTO test_table (text_field, number_field) VALUES ('test_one', 1)")
    executeSql("INSERT INTO test_table (text_field, number_field) VALUES ('test_two', 2)")
    executeSql("INSERT INTO test_table (text_field, number_field) VALUES ('test_three', 3)")

    val result = subject.update("UPDATE test_table SET text_field = 'test_four', number_field = 4 WHERE text_field = ? OR number_field = ?",
                                "test_one", 2)
    result should be(2)
  }

  test("should not execute update with incorrect number of arguments") {
    intercept[SQLException] {
      subject.update("INSERT INTO test_table (text_field, number_field) VALUES (?, ?)")
    }
  }

  private def newDataSource(url: String, user: String = "sa", password: String = "sa") = {
    val ds = new JdbcDataSource()
    ds.setURL(url)
    ds.setUser(user)
    ds.setPassword(password)
    ds
  }

  private def executeSql(sql: String) {
    val conn = ds.getConnection
    val stmt = conn.createStatement()
    stmt.execute(sql)
  }

  private class TestRowMapper extends RowMapper[(String, Int)] {
    def map(rs: ResultSet) = (rs.getString("text_field"), rs.getInt("number_field"))
  }
}
