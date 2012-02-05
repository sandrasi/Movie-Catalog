package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.JavaConversions._
import scala.collection.mutable.{Map => MutableMap}
import java.lang.IllegalStateException
import java.util.Locale
import java.util.Locale.US
import org.neo4j.graphdb.Direction._
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.LongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.AbstractCast
import com.github.sandrasi.moviecatalog.domain.entities.container._
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.DigitalContainerRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.EntityRelationshipType.IsA
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.PropertyManager._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.CharacterRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.FilmCrewRelationshipType
import org.neo4j.graphdb.{NotFoundException, GraphDatabaseService, Node}

private[neo4j] class NodeManager private (db: GraphDatabaseService) extends MovieCatalogGraphPropertyNames {

  Validate.notNull(db)
  
  private final val SubrefNodeSupp = SubreferenceNodeSupport(db)

  def createNodeFrom(e: LongIdEntity)(implicit l: Locale = US): Node = e match {
    case ac: AbstractCast => connectNodeToSubreferenceNode(setNodePropertiesFrom(createNode(ac), ac), e.getClass)
    case c: Character => connectNodeToSubreferenceNode(setNodePropertiesFrom(createNode(c), c), e.getClass)
    case dc: DigitalContainer => connectNodeToSubreferenceNode(setNodePropertiesFrom(createNode(dc), dc), e.getClass)
    case m: Movie => connectNodeToSubreferenceNode(setNodePropertiesFrom(createNode(m), m), e.getClass)
    case p: Person => connectNodeToSubreferenceNode(setNodePropertiesFrom(createNode(p), p), e.getClass)
    case s: Soundtrack => connectNodeToSubreferenceNode(setNodePropertiesFrom(createNode(s), s, l), e.getClass)
    case s: Subtitle => connectNodeToSubreferenceNode(setNodePropertiesFrom(createNode(s), s, l), e.getClass)
    case _ => throw new IllegalArgumentException("Unsupported entity type: %s".format(e.getClass.getName))
  }
  
  def updateNodeOf(e: LongIdEntity)(implicit l: Locale = US): Node = e match {
    case ac: AbstractCast => setNodePropertiesFrom(getNode(ac), ac)
    case c: Character => setNodePropertiesFrom(getNode(c), c)
    case dc: DigitalContainer => setNodePropertiesFrom(getNode(dc), dc)
    case m: Movie => setNodePropertiesFrom(getNode(m), m)
    case p: Person => setNodePropertiesFrom(getNode(p), p)
    case s: Soundtrack => setNodePropertiesFrom(getNode(s), s, l)
    case s: Subtitle => setNodePropertiesFrom(getNode(s), s, l)
    case _ => throw new IllegalArgumentException("Unsupported entity type: %s".format(e.getClass.getName))
  }

  private def setNodePropertiesFrom(n: Node, ac: AbstractCast): Node = {
    n.getRelationships(FilmCrewRelationshipType.forClass(ac.getClass), OUTGOING).foreach(_.delete())
    n.getRelationships(PlayedBy, OUTGOING).foreach(_.delete())
    n.getRelationships(AppearedIn, OUTGOING).foreach(_.delete())
    n.createRelationshipTo(getNode(ac.person), FilmCrewRelationshipType.forClass(ac.getClass))
    n.createRelationshipTo(getNode(ac.character), PlayedBy)
    n.createRelationshipTo(getNode(ac.motionPicture), AppearedIn)
    n
  }

  private def setNodePropertiesFrom(n: Node, c: Character): Node = {
    setString(n, CharacterName, c.name)
    setString(n, CharacterDiscriminator, c.discriminator)
    n
  }

  private def setNodePropertiesFrom(n: Node, dc: DigitalContainer): Node = {
    n.getRelationships(StoredIn, INCOMING).foreach(_.delete())
    n.getRelationships(WithSoundtrack, OUTGOING).foreach(_.delete())
    n.getRelationships(WithSubtitle, OUTGOING).foreach(_.delete())
    getNode(dc.motionPicture).createRelationshipTo(n, StoredIn)
    dc.soundtracks.map(getNode(_)).foreach(n.createRelationshipTo(_, WithSoundtrack))
    dc.subtitles.map(getNode(_)).foreach(n.createRelationshipTo(_, WithSubtitle))
    n
  }

  private def setNodePropertiesFrom(n: Node, m: Movie): Node = {
    setLocalizedText(n, MovieOriginalTitle, m.originalTitle)
    setLocalizedText(n, MovieLocalizedTitles, m.localizedTitles)
    setDuration(n, MovieLength, m.length)
    setLocalDate(n, MovieReleaseDate, m.releaseDate)
    n
  }

  private def setNodePropertiesFrom(n: Node, p: Person): Node = {
    setString(n, PersonName, p.name);
    setString(n, PersonGender, p.gender.toString);
    setLocalDate(n, PersonDateOfBirth, p.dateOfBirth)
    setString(n, PersonPlaceOfBirth, p.placeOfBirth)
    n
  }

  private def setNodePropertiesFrom(n: Node, s: Soundtrack, l: Locale): Node = {
    if ((s.languageName != None) && (s.languageName.get.locale != l)) throw new IllegalStateException("Soundtrack language name locale " + s.languageName.get.locale + " does not match the current locale " + l)
    if ((s.formatName != None) && (s.formatName.get.locale != l)) throw new IllegalStateException("Soundtrack format name locale " + s.formatName.get.locale + " does not match the current locale " + l)
    setString(n, SoundtrackLanguageCode, s.languageCode)
    setString(n, SoundtrackFormatCode, s.formatCode)
    if (s.languageName != None) addOrReplaceLocalizedText(n, SoundtrackLanguageNames, s.languageName.get) else deleteLocalizedText(n, SoundtrackLanguageNames, l)
    if (s.formatName != None) addOrReplaceLocalizedText(n, SoundtrackFormatNames, s.formatName.get) else deleteLocalizedText(n, SoundtrackFormatNames, l)
    n
  }

  private def setNodePropertiesFrom(n: Node, s: Subtitle, l: Locale): Node = {
    if ((s.languageName != None) && (s.languageName.get.locale != l)) throw new IllegalStateException("Soundtrack language name locale " + s.languageName.get.locale + " does not match the current locale " + l)
    setString(n, SubtitleLanguageCode, s.languageCode)
    if (s.languageName != None) addOrReplaceLocalizedText(n, SubtitleLanguageNames, s.languageName.get) else deleteLocalizedText(n, SubtitleLanguageNames, l)
    n
  }

  private def connectNodeToSubreferenceNode[A <: LongIdEntity](n: Node, c: Class[A]): Node = { n.createRelationshipTo(db.getNodeById(SubrefNodeSupp.getSubrefNodeIdFor(c)), IsA); n }

  private def createNode(e: LongIdEntity): Node = if (e.id == None) db.createNode() else throw new IllegalStateException("Entity %s already has an id: %d".format(e.getClass.getName, e.id.get))

  private def getNode(e: LongIdEntity) = try {
    val node = if (e.id != None) db.getNodeById(e.id.get) else throw new IllegalStateException("%s is not in the database".format(e))
    if (SubrefNodeSupp.isNodeOfType(node, e.getClass)) node else throw new ClassCastException("Node [id: %d] is not of type %s".format(e.id.get, e.getClass.getName))
  } catch {
    case _: NotFoundException => throw new IllegalStateException("%s is not in the database".format(e))
  }
}

private[neo4j] object NodeManager {

  private final val Instances = MutableMap.empty[GraphDatabaseService, NodeManager]

  def apply(db: GraphDatabaseService): NodeManager = {
    if (!Instances.contains(db)) {
      Instances += db -> new NodeManager(db)
    }
    Instances(db)
  }
}
