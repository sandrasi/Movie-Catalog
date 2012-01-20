package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.mutable.{Map => MutableMap}
import org.neo4j.graphdb.{GraphDatabaseService, Node}
import org.neo4j.graphdb.index.Index
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.LongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText

private[neo4j] class IndexManager private (db: GraphDatabaseService) extends MovieCatalogGraphPropertyNames {

  Validate.notNull(db)

//  private final val ClassLocale = classOf[Locale]

//  def index(n: Node, nodeType: Class[_]) {
//    nodeType match {
//      case ClassLocale => indexLocale(n)
//      case _ => throw new IllegalArgumentException("Unsupported node type: %s".format(nodeType.getName))
//    }
//  }

  private def reindexNodeProperty(idx: Index[Node], n: Node, p: String) { idx.remove(n, p); idx.add(n, p, n.getProperty(p)) }

  def exists(e: LongIdEntity): Boolean = getNodeFor(e) != None
  
  def getNodeFor(e: LongIdEntity): Option[Node] = e match {
    case lt: LocalizedText => getNodeForLocalizedText(lt)
    case _ => throw new IllegalArgumentException("Unsupported entity type: %s".format(e.getClass.getName))
  }

  private def getNodeForLocalizedText(lt: LocalizedText) = {
//    val tq = new TermQuery(new Term(LocalizedTextText, lt.text))
//    val node = db.index().forNodes(classOf[LocalizedText].getName).query(tq)
    //asOption(node)
    None
  }
  
  private def asOption(n: Node) = if (n != null) Some(n) else None
}

private[neo4j] object IndexManager {

  private final val Instances = MutableMap.empty[GraphDatabaseService, IndexManager]

  def apply(db: GraphDatabaseService): IndexManager = {
    if (!Instances.contains(db)) {
      Instances += db -> new IndexManager(db)
    }
    Instances(db)
  }
}
