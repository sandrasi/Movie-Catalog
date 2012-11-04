package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

import com.github.sandrasi.moviecatalog.common.Enum

trait AbstractRelationshipType[A] extends Enum[A] {

  trait RelationshipType extends Value with org.neo4j.graphdb.RelationshipType { self: A =>

    override def name = toString
  }
}
