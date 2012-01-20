package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.mutable.{Map => MutableMap}
import java.util.Locale
import java.util.Locale.US
import org.neo4j.graphdb.{GraphDatabaseService, Node, NotFoundException}
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.LongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText
import com.github.sandrasi.moviecatalog.domain.entities.container._
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.DigitalContainerRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.EntityRelationshipType.IsA
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.LocalizedTextManager.setLocalizedTextOf

private[neo4j] class NodeFactory private (db: GraphDatabaseService) extends MovieCatalogGraphPropertyNames {

  Validate.notNull(db)
  
  private final val SubrefNodeSupp = SubreferenceNodeSupport(db)

  def createNodeFrom(e: LongIdEntity): Node = e match {
    case c: Character => createNodeFrom(c)
    case dc: DigitalContainer => createNodeFrom(dc)
    case m: Movie => createNodeFrom(m)
    case p: Person => createNodeFrom(p)
    case s: Soundtrack => createNodeFrom(s)
    case s: Subtitle => createNodeFrom(s)
    case _ => throw new IllegalArgumentException("Unsupported entity type: %s".format(e.getClass.getName))
  }
  
  private def createNodeFrom(c: Character): Node = {
    val characterNode = createNodeForEntity(c)
    characterNode.setProperty(CharacterName, c.name)
    characterNode.setProperty(CharacterDiscriminator, c.discriminator)
    connectNodeToSubreferenceNode(characterNode, classOf[Character])
    characterNode
  }
  
  private def createNodeFrom(dc: DigitalContainer): Node = {
    val digitalContainerNode = createNodeForEntity(dc)
    getNodeOf(dc.motionPicture).createRelationshipTo(digitalContainerNode, StoredIn)
    dc.soundtracks.map(getNodeOf(_)).foreach(digitalContainerNode.createRelationshipTo(_, WithSoundtrack))
    dc.subtitles.map(getNodeOf(_)).foreach(digitalContainerNode.createRelationshipTo(_, WithSubtitle))
    connectNodeToSubreferenceNode(digitalContainerNode, classOf[DigitalContainer])
    digitalContainerNode
  }

  private def getNodeOf(e: LongIdEntity) = try {
    val node = if (e.id != None) db.getNodeById(e.id.get) else throw new IllegalStateException("%s is not in the database".format(e))
    if (SubrefNodeSupp.isNodeOfType(node, e.getClass)) node else throw new ClassCastException("Node [id: %d] is not of type %s".format(e.id.get, e.getClass.getName))
  } catch {
    case _: NotFoundException => throw new IllegalStateException("%s is not in the database".format(e))
  }

  private def createNodeFrom(m: Movie): Node = {
    val movieNode = createNodeForEntity(m)
    movieNode.setProperty(MovieLength, m.length.getMillis)
    movieNode.setProperty(MovieReleaseDate, m.releaseDate.toDateTimeAtStartOfDay.getMillis)
    setLocalizedTextOf(movieNode, MovieOriginalTitle, m.originalTitle)
    setLocalizedTextOf(movieNode, MovieLocalizedTitles, m.localizedTitles)
    connectNodeToSubreferenceNode(movieNode, classOf[Movie])
    movieNode
  }
  
  private def createNodeFrom(p: Person): Node = {
    val personNode = createNodeForEntity(p)
    personNode.setProperty(PersonName, p.name);
    personNode.setProperty(PersonGender, p.gender.toString);
    personNode.setProperty(PersonDateOfBirth, p.dateOfBirth.toDateTimeAtStartOfDay.getMillis)
    personNode.setProperty(PersonPlaceOfBirth, p.placeOfBirth)
    connectNodeToSubreferenceNode(personNode, classOf[Person])
    personNode
  }

  private def createNodeFrom(s: Soundtrack): Node = {
    val soundtrackNode = createNodeForEntity(s)
    soundtrackNode.setProperty(SoundtrackLanguageCode, s.languageCode)
    soundtrackNode.setProperty(SoundtrackFormatCode, s.formatCode)
    if (s.languageName != None) setLocalizedTextOf(soundtrackNode, SoundtrackLanguageNames, s.languageName.get)
    if (s.formatName != None) setLocalizedTextOf(soundtrackNode, SoundtrackFormatNames, s.formatName.get)
    connectNodeToSubreferenceNode(soundtrackNode, classOf[Soundtrack])
    soundtrackNode
  }

  private def createNodeFrom(s: Subtitle): Node = {
    val subtitleNode = createNodeForEntity(s)
    subtitleNode.setProperty(SubtitleLanguageCode, s.languageCode)
    if (s.languageName != None) setLocalizedTextOf(subtitleNode, SubtitleLanguageNames, s.languageName.get)
    connectNodeToSubreferenceNode(subtitleNode, classOf[Subtitle])
    subtitleNode
  }
  
  private def createNodeForEntity(e: LongIdEntity): Node = if (e.id == None) db.createNode() else throw new IllegalStateException("Entity %s already has an id: %d".format(e.getClass.getName, e.id.get))
  
  private def connectNodeToSubreferenceNode[A <: LongIdEntity](n: Node, c: Class[A]) { n.createRelationshipTo(db.getNodeById(SubrefNodeSupp.getSubrefNodeIdFor(c)), IsA) }
  
  def updateNodeOf(e: LongIdEntity)(implicit locale: Locale = US): Node = e match {
    case s: Soundtrack => updateNodeOf(s, locale)
    case _ => throw new IllegalArgumentException("Unsupported entity type: %s".format(e.getClass.getName))
  }
  
  private def updateNodeOf(s: Soundtrack, l: Locale): Node = {
    val soundtrackNode = getNodeOf(s)
    soundtrackNode.setProperty(SoundtrackLanguageCode, s.languageCode)
    soundtrackNode.setProperty(SoundtrackFormatCode, s.formatCode)
    //if (s.languageName != None) addLocalizedTextProperty(soundtrackNode, SoundtrackLanguageNames, Array(s.languageName.get)) else removeLocalizedTextProperty(soundtrackNode, SoundtrackLanguageNames, l)
    soundtrackNode
  }
  
  private def addLocalizedTextProperty(n: Node,  k: String, lta: Array[LocalizedText]) {

  }
}

private[neo4j] object NodeFactory {

  private final val Instances = MutableMap.empty[GraphDatabaseService, NodeFactory]

  def apply(db: GraphDatabaseService): NodeFactory = {
    if (!Instances.contains(db)) {
      Instances += db -> new NodeFactory(db)
    }
    Instances(db)
  }
}
