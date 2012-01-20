package com.github.sandrasi.moviecatalog.repository.jdbc

import java.sql.ResultSet

trait RowMapper[A] {

  def map(rs: ResultSet): A
}
