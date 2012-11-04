package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

import collection.immutable.ListMap

trait AbstractClassBasedRelationshipType[A] extends AbstractRelationshipType[A] {

  private var _values = ListMap.empty[Class[_], A]

  def forClass[B](c: Class[B]): A = if (_values.contains(c)) _values(c) else throw new NoSuchElementException("%s is not bound to any value".format(c.getName))

  trait ClassBasedRelationshipType extends RelationshipType { self: A =>

    if (!_values.contains(forClass)) _values += forClass -> this else throw new IllegalStateException("%s is already bound to %s".format(forClass.getName, _values(forClass)))

    def forClass: Class[_]

    override def name = forClass.getName
  }
}
