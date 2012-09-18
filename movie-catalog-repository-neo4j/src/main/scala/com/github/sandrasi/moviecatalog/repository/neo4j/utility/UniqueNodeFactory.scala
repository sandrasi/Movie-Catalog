package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MutableMap}
import org.neo4j.graphdb._
import org.neo4j.graphdb.Direction._
import java.util.Locale
import java.util.Locale._
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.AbstractCast
import com.github.sandrasi.moviecatalog.domain.entities.container.{Subtitle, DigitalContainer, Soundtrack}
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.CharacterRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.DigitalContainerRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.FilmCrewRelationshipType
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.MovieCatalogDbConstants._
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.PropertyManager._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.EntityRelationshipType._

private[neo4j] class UniqueNodeFactory(db: GraphDatabaseService) {

  Validate.notNull(db)

  private final val DbMgr = DatabaseManager(db)
  private final val IdxMgr = IndexManager(db)

  def createNodeFrom(e: VersionedLongIdEntity)(implicit tx: Transaction, l: Locale = US): Node = lock(e) { e match {
    case ac: AbstractCast => connectNodeToSubreferenceNode(connectNodeToSubreferenceNode(setNodePropertiesFrom(DbMgr.createNodeFor(ac), ac), ac.getClass), classOf[AbstractCast])
    case c: Character => connectNodeToSubreferenceNode(setNodePropertiesFrom(DbMgr.createNodeFor(c), c), classOf[Character])
    case dc: DigitalContainer => connectNodeToSubreferenceNode(setNodePropertiesFrom(DbMgr.createNodeFor(dc), dc), classOf[DigitalContainer])
    case m: Movie => connectNodeToSubreferenceNode(setNodePropertiesFrom(DbMgr.createNodeFor(m), m), classOf[Movie])
    case p: Person => connectNodeToSubreferenceNode(setNodePropertiesFrom(DbMgr.createNodeFor(p), p), classOf[Person])
    case s: Soundtrack => connectNodeToSubreferenceNode(setNodePropertiesFrom(DbMgr.createNodeFor(s), s, l), classOf[Soundtrack])
    case s: Subtitle => connectNodeToSubreferenceNode(setNodePropertiesFrom(DbMgr.createNodeFor(s), s, l), classOf[Subtitle])
  }}

  private def lock(e: VersionedLongIdEntity)(dbOp: => Node)(implicit tx: Transaction): Node = {
    tx.acquireWriteLock(DbMgr.getSubreferenceNode(e.getClass))
    dbOp
  }

  private def connectNodeToSubreferenceNode[A <: VersionedLongIdEntity](n: Node, c: Class[A]): Node = { n.createRelationshipTo(DbMgr.getSubreferenceNode(c), IsA); n }

  private def setNodePropertiesFrom(n: Node, ac: AbstractCast): Node = withExistenceCheck(ac) {
    n.getRelationships(FilmCrewRelationshipType.forClass(ac.getClass), OUTGOING).asScala.foreach(_.delete())
    n.getRelationships(Played, OUTGOING).asScala.foreach(_.delete())
    n.getRelationships(AppearedIn, OUTGOING).asScala.foreach(_.delete())
    n.createRelationshipTo(DbMgr.getNodeOf(ac.person), FilmCrewRelationshipType.forClass(ac.getClass))
    n.createRelationshipTo(DbMgr.getNodeOf(ac.character), Played)
    n.createRelationshipTo(DbMgr.getNodeOf(ac.motionPicture), AppearedIn)
    setVersion(n, ac)
    IdxMgr.index(n, ac)
    n
  }

  private def setNodePropertiesFrom(n: Node, c: Character): Node = withExistenceCheck(c) {
    setString(n, CharacterName, c.name)
    setString(n, CharacterDiscriminator, c.discriminator)
    setVersion(n, c)
    IdxMgr.index(n, c)
    n
  }

  private def setNodePropertiesFrom(n: Node, dc: DigitalContainer): Node = withExistenceCheck(dc) {
    n.getRelationships(WithContent, OUTGOING).asScala.foreach(_.delete())
    n.getRelationships(WithSoundtrack, OUTGOING).asScala.foreach(_.delete())
    n.getRelationships(WithSubtitle, OUTGOING).asScala.foreach(_.delete())
    n.createRelationshipTo(DbMgr.getNodeOf(dc.motionPicture), WithContent)
    dc.soundtracks.map(DbMgr.getNodeOf(_)).foreach(n.createRelationshipTo(_, WithSoundtrack))
    dc.subtitles.map(DbMgr.getNodeOf(_)).foreach(n.createRelationshipTo(_, WithSubtitle))
    setVersion(n, dc)
    IdxMgr.index(n, dc)
    n
  }

  private def setNodePropertiesFrom(n: Node, m: Movie): Node = withExistenceCheck(m) {
    setLocalizedText(n, MovieOriginalTitle, m.originalTitle)
    setLocalizedText(n, MovieLocalizedTitles, m.localizedTitles)
    setDuration(n, MovieRuntime, m.runtime)
    setLocalDate(n, MovieReleaseDate, m.releaseDate)
    setVersion(n, m)
    IdxMgr.index(n, m)
    n
  }

  private def setNodePropertiesFrom(n: Node, p: Person): Node = withExistenceCheck(p) {
    setString(n, PersonName, p.name)
    setString(n, PersonGender, p.gender.toString)
    setLocalDate(n, PersonDateOfBirth, p.dateOfBirth)
    setString(n, PersonPlaceOfBirth, p.placeOfBirth)
    setVersion(n, p)
    IdxMgr.index(n, p)
    n
  }

  private def setNodePropertiesFrom(n: Node, s: Soundtrack, l: Locale): Node = withExistenceCheck(s) {
    if ((s.languageName != None) && (s.languageName.get.locale != l)) throw new IllegalStateException("Soundtrack language name locale %s does not match the current locale %s".format(s.languageName.get.locale, l))
    if ((s.formatName != None) && (s.formatName.get.locale != l)) throw new IllegalStateException("Soundtrack format name locale %s does not match the current locale %s".format(s.formatName.get.locale, l))
    setString(n, SoundtrackLanguageCode, s.languageCode)
    setString(n, SoundtrackFormatCode, s.formatCode)
    if (s.languageName != None) addOrReplaceLocalizedText(n, SoundtrackLanguageNames, s.languageName.get) else deleteLocalizedText(n, SoundtrackLanguageNames, l)
    if (s.formatName != None) addOrReplaceLocalizedText(n, SoundtrackFormatNames, s.formatName.get) else deleteLocalizedText(n, SoundtrackFormatNames, l)
    setVersion(n, s)
    IdxMgr.index(n, s)
    n
  }

  private def setNodePropertiesFrom(n: Node, s: Subtitle, l: Locale): Node = withExistenceCheck(s) {
    if ((s.languageName != None) && (s.languageName.get.locale != l)) throw new IllegalStateException("Subtitle language name locale %s does not match the current locale %s".format(s.languageName.get.locale, l))
    setString(n, SubtitleLanguageCode, s.languageCode)
    if (s.languageName != None) addOrReplaceLocalizedText(n, SubtitleLanguageNames, s.languageName.get) else deleteLocalizedText(n, SubtitleLanguageNames, l)
    setVersion(n, s)
    IdxMgr.index(n, s)
    n
  }

  private def withExistenceCheck(e: VersionedLongIdEntity)(dbOp: => Node) = if (!IdxMgr.exists(e)) dbOp else throw new IllegalArgumentException("Entity %s already exists".format(e))

  private def setVersion(n: Node, e: VersionedLongIdEntity) {
    if (e.id != None && !hasExpectedVersion(n, e.version)) throw new IllegalStateException("%s is out of date".format(e))
    setLong(n, Version, if (e.id == None) e.version else e.version + 1)
  }

  private def hasExpectedVersion(n: Node, v: Long) = hasLong(n, Version) && getLong(n, Version) == v
}

private[neo4j] object UniqueNodeFactory {

  private final val Instances = MutableMap.empty[GraphDatabaseService, UniqueNodeFactory]

  def apply(db: GraphDatabaseService): UniqueNodeFactory = {
    if (!Instances.contains(db)) {
      Instances += db -> new UniqueNodeFactory(db)
    }
    Instances(db)
  }
}
