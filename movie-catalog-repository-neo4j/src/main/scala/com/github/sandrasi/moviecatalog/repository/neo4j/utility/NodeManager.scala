package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.JavaConversions._
import scala.collection.mutable.{Map => MutableMap}
import java.lang.IllegalStateException
import java.util.Locale
import java.util.Locale.US
import org.neo4j.graphdb.Direction._
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
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

  def createNodeFrom(e: VersionedLongIdEntity)(implicit l: Locale = US): Node = e match {
    case ac: AbstractCast => connectNodeToSubreferenceNode(setNodePropertiesFrom(createNode(ac), ac), e.getClass)
    case c: Character => connectNodeToSubreferenceNode(setNodePropertiesFrom(createNode(c), c), e.getClass)
    case dc: DigitalContainer => connectNodeToSubreferenceNode(setNodePropertiesFrom(createNode(dc), dc), e.getClass)
    case m: Movie => connectNodeToSubreferenceNode(setNodePropertiesFrom(createNode(m), m), e.getClass)
    case p: Person => connectNodeToSubreferenceNode(setNodePropertiesFrom(createNode(p), p), e.getClass)
    case s: Soundtrack => connectNodeToSubreferenceNode(setNodePropertiesFrom(createNode(s), s, l), e.getClass)
    case s: Subtitle => connectNodeToSubreferenceNode(setNodePropertiesFrom(createNode(s), s, l), e.getClass)
    case _ => throw new IllegalArgumentException("Unsupported entity type: %s".format(e.getClass.getName))
  }

  private def connectNodeToSubreferenceNode[A <: VersionedLongIdEntity](n: Node, c: Class[A]): Node = { n.createRelationshipTo(db.getNodeById(SubrefNodeSupp.getSubrefNodeIdFor(c)), IsA); n }

  private def createNode(e: VersionedLongIdEntity): Node = if (e.id == None) db.createNode() else throw new IllegalStateException("Entity %s already has an id: %d".format(e, e.id.get))

  def updateNodeOf(e: VersionedLongIdEntity)(implicit l: Locale = US): Node = e match {
    case ac: AbstractCast => setNodePropertiesFrom(getNode(ac), ac)
    case c: Character => setNodePropertiesFrom(getNode(c), c)
    case dc: DigitalContainer => setNodePropertiesFrom(getNode(dc), dc)
    case m: Movie => setNodePropertiesFrom(getNode(m), m)
    case p: Person => setNodePropertiesFrom(getNode(p), p)
    case s: Soundtrack => setNodePropertiesFrom(getNode(s), s, l)
    case s: Subtitle => setNodePropertiesFrom(getNode(s), s, l)
    case _ => throw new IllegalArgumentException("Unsupported entity type: %s".format(e.getClass.getName))
  }

  private def getNode(e: VersionedLongIdEntity) = try {
    val node = if (e.id != None) db.getNodeById(e.id.get) else throw new IllegalStateException("%s is not in the database".format(e))
    if (SubrefNodeSupp.isNodeOfType(node, e.getClass)) node else throw new ClassCastException("Node [id: %d] is not of type %s".format(e.id.get, e.getClass.getName))
  } catch {
    case _: NotFoundException => throw new IllegalStateException("%s is not in the database".format(e))
  }

  private def setNodePropertiesFrom(n: Node, ac: AbstractCast): Node = {
    n.getRelationships(FilmCrewRelationshipType.forClass(ac.getClass), OUTGOING).foreach(_.delete())
    n.getRelationships(Played, OUTGOING).foreach(_.delete())
    n.getRelationships(AppearedIn, OUTGOING).foreach(_.delete())
    n.createRelationshipTo(getNode(ac.person), FilmCrewRelationshipType.forClass(ac.getClass))
    n.createRelationshipTo(getNode(ac.character), Played)
    n.createRelationshipTo(getNode(ac.motionPicture), AppearedIn)
    setVersion(n, ac)
    n
  }

  private def setNodePropertiesFrom(n: Node, c: Character): Node = {
    setString(n, CharacterName, c.name)
    setString(n, CharacterDiscriminator, c.discriminator)
    setVersion(n, c)
    n
  }

  private def setNodePropertiesFrom(n: Node, dc: DigitalContainer): Node = {
    n.getRelationships(WithContent, OUTGOING).foreach(_.delete())
    n.getRelationships(WithSoundtrack, OUTGOING).foreach(_.delete())
    n.getRelationships(WithSubtitle, OUTGOING).foreach(_.delete())
    n.createRelationshipTo(getNode(dc.motionPicture), WithContent)
    dc.soundtracks.map(getNode(_)).foreach(n.createRelationshipTo(_, WithSoundtrack))
    dc.subtitles.map(getNode(_)).foreach(n.createRelationshipTo(_, WithSubtitle))
    setVersion(n, dc)
    n
  }

  private def setNodePropertiesFrom(n: Node, m: Movie): Node = {
    setLocalizedText(n, MovieOriginalTitle, m.originalTitle)
    setLocalizedText(n, MovieLocalizedTitles, m.localizedTitles)
    setDuration(n, MovieLength, m.length)
    setLocalDate(n, MovieReleaseDate, m.releaseDate)
    setVersion(n, m)
    n
  }

  private def setNodePropertiesFrom(n: Node, p: Person): Node = {
    setString(n, PersonName, p.name);
    setString(n, PersonGender, p.gender.toString);
    setLocalDate(n, PersonDateOfBirth, p.dateOfBirth)
    setString(n, PersonPlaceOfBirth, p.placeOfBirth)
    setVersion(n, p)
    n
  }

  private def setNodePropertiesFrom(n: Node, s: Soundtrack, l: Locale): Node = {
    if ((s.languageName != None) && (s.languageName.get.locale != l)) throw new IllegalStateException("Soundtrack language name locale %s does not match the current locale %s".format(s.languageName.get.locale, l))
    if ((s.formatName != None) && (s.formatName.get.locale != l)) throw new IllegalStateException("Soundtrack format name locale %s does not match the current locale %s".format(s.formatName.get.locale, l))
    setString(n, SoundtrackLanguageCode, s.languageCode)
    setString(n, SoundtrackFormatCode, s.formatCode)
    if (s.languageName != None) addOrReplaceLocalizedText(n, SoundtrackLanguageNames, s.languageName.get) else deleteLocalizedText(n, SoundtrackLanguageNames, l)
    if (s.formatName != None) addOrReplaceLocalizedText(n, SoundtrackFormatNames, s.formatName.get) else deleteLocalizedText(n, SoundtrackFormatNames, l)
    setVersion(n, s)
    n
  }

  private def setNodePropertiesFrom(n: Node, s: Subtitle, l: Locale): Node = {
    if ((s.languageName != None) && (s.languageName.get.locale != l)) throw new IllegalStateException("Subtitle language name locale %s does not match the current locale %s".format(s.languageName.get.locale, l))
    setString(n, SubtitleLanguageCode, s.languageCode)
    if (s.languageName != None) addOrReplaceLocalizedText(n, SubtitleLanguageNames, s.languageName.get) else deleteLocalizedText(n, SubtitleLanguageNames, l)
    setVersion(n, s)
    n
  }

  private def setVersion(n: Node, e: VersionedLongIdEntity) {
    if (e.id != None && !hasExpectedVersion(n, e.version)) throw new IllegalStateException("%s is out of date".format(e))
    setLong(n, Version, if (e.id == None) e.version else e.version + 1)
  }
  
  private def hasExpectedVersion(n: Node, v: Long) = hasLong(n, Version) && getLong(n, Version) == v

  def deleteNodeOf(e: VersionedLongIdEntity) {
    e match {
      case _: AbstractCast | _: Character | _: DigitalContainer | _: Movie | _: Person | _: Soundtrack | _: Subtitle => deleteNodeWithRelationships(e)
      case _ => throw new IllegalArgumentException("Unsupported entity type: %s".format(e.getClass.getName))
    }
  }

  private def deleteNodeWithRelationships(e: VersionedLongIdEntity) {
    val n = getNode(e)
    if (n.hasRelationship(INCOMING)) throw new IllegalStateException("%s is referenced by other entities".format(e))
    if (!hasExpectedVersion(n, e.version)) throw new IllegalStateException("%s is out of date".format(e))
    n.getRelationships(OUTGOING).foreach(_.delete())
    n.delete()
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
