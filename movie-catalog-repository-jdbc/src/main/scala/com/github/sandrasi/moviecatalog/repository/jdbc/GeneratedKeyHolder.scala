package com.github.sandrasi.moviecatalog.repository.jdbc

import collection.mutable.ListBuffer

class GeneratedKeyHolder {

  private[jdbc] val keyBuffer = new ListBuffer[Long]()

  def keys(): List[Long] = keyBuffer.toList
}
