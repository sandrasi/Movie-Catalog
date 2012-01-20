package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

import org.neo4j.graphdb.{Relationship, RelationshipType}

private[neo4j] abstract class AbstractRelationshipType extends Enumeration {

  protected def relationshipTypeValue(name: String): RelationshipTypeValue = new RelationshipTypeValue(name)

  class RelationshipTypeValue(override val name: String) extends Val(name) with RelationshipType
}

private[neo4j] object AbstractRelationshipType {

  def isRelationshipOfType(r: Relationship, relationshipType: RelationshipType) = r.getType == relationshipType
}
