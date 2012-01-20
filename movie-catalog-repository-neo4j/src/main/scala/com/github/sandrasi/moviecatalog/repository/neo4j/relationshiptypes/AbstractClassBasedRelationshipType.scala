package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

private[neo4j] abstract class AbstractClassBasedRelationshipType[A] extends AbstractRelationshipType {

  def forClass[B <: A](c: Class[B]): RelationshipTypeValue = withName(c.getName).asInstanceOf[RelationshipTypeValue]

  protected def relationshipTypeValue[B <: A](c: Class[B]): RelationshipTypeValue = relationshipTypeValue(c.getName)
}
