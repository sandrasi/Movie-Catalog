package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MutableMap}
import org.neo4j.graphdb.{GraphDatabaseService, Node}
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.AbstractCast
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.FilmCrewRelationshipType
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.CharacterRelationshipType._
import org.neo4j.graphdb.Direction._
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.MovieCatalogDbConstants._
import com.github.sandrasi.moviecatalog.domain.entities.container.{DigitalContainer, Soundtrack, Subtitle}
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.DigitalContainerRelationshipType._
import com.github.sandrasi.moviecatalog.domain.entities.core.{Person, Character, MotionPicture}
import org.neo4j.index.lucene.ValueContext
import org.apache.lucene.search.{NumericRangeQuery, TermQuery, BooleanQuery}
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause.Occur._
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity

private[utility] class IndexManager(db: GraphDatabaseService) {

  Validate.notNull(db)

  private final val IdxMgr = db.index()
  private final val CastIndex = IdxMgr.forNodes("Cast")
  private final val CharacterIndex = IdxMgr.forNodes("Characters")
  private final val DigitalContainerIndex = IdxMgr.forNodes("DigitalConainers")
  private final val MotionPictureIndex = IdxMgr.forNodes("MotionPicture") // TODO (sandrasi): separate index for movies and tv shows
  private final val PersonIndex = IdxMgr.forNodes("Persons")
  private final val SoundtrackIndex = IdxMgr.forNodes("Soundtracks")
  private final val SubtitleIndex = IdxMgr.forNodes("Subtitles")

  def index: PartialFunction[(Node, VersionedLongIdEntity), Unit] = {
    case (n, ac: AbstractCast) => index(n, ac)
    case (n, c: Character) => index(n, c)
    case (n, dc: DigitalContainer) => index(n, dc)
    case (n, m: MotionPicture) => index(n, m)
    case (n, p: Person) => index(n, p)
    case (n, s: Soundtrack) => index(n, s)
    case (n, s: Subtitle) => index(n, s)
  }

  private def index(n: Node, ac: AbstractCast) {
    CastIndex.remove(n)
    CastIndex.add(n, "person", lookUpExact(ac.person).get.getId)
    CastIndex.add(n, "character", lookUpExact(ac.character).get.getId)
    CastIndex.add(n, "motionPicture", lookUpExact(ac.motionPicture).get.getId)
  }

  private def index(n: Node, c: Character) {
    CharacterIndex.remove(n)
    CharacterIndex.add(n, CharacterName, c.name)
    CharacterIndex.add(n, CharacterDiscriminator, c.discriminator)
  }

  private def index(n: Node, dc: DigitalContainer) {
    DigitalContainerIndex.remove(n)
    DigitalContainerIndex.add(n, "motionPicture", lookUpExact(dc.motionPicture).get.getId)
    dc.soundtracks.foreach((s: Soundtrack) => DigitalContainerIndex.add(n, "soundtrack", lookUpExact(s).get.getId))
    dc.subtitles.foreach((s: Subtitle) => DigitalContainerIndex.add(n, "subtitle", lookUpExact(s).get.getId))
  }

  private def index(n: Node, m: MotionPicture) {
    MotionPictureIndex.remove(n)
    MotionPictureIndex.add(n, MovieOriginalTitle, m.originalTitle.text)
    MotionPictureIndex.add(n, MovieOriginalTitle + LocaleLanguage, m.originalTitle.locale.getLanguage)
    MotionPictureIndex.add(n, MovieOriginalTitle + LocaleCountry, m.originalTitle.locale.getCountry)
    MotionPictureIndex.add(n, MovieOriginalTitle + LocaleVariant, m.originalTitle.locale.getVariant)
    // TODO (sandrasi): Index the motion picture by localized titles and runtime when needed
    //for (lt <- m.localizedTitles) {
    //  MovieIndex.add(n, MovieLocalizedTitles, lt.text)
    //  MovieIndex.add(n, MovieLocalizedTitles + LocaleLanguage, lt.locale.getLanguage)
    //  MovieIndex.add(n, MovieLocalizedTitles + LocaleCountry, lt.locale.getCountry)
    //  MovieIndex.add(n, MovieLocalizedTitles + LocaleVariant, lt.locale.getVariant)
    //}
    //MovieIndex.add(n, MovieRuntime, ValueContext.numeric(m.runtime.getMillis))
    MotionPictureIndex.add(n, MovieReleaseDate, ValueContext.numeric(m.releaseDate.toDateTimeAtStartOfDay.getMillis))
  }

  private def index(n: Node, p: Person) {
    PersonIndex.remove(n)
    PersonIndex.add(n, PersonName, p.name)
    PersonIndex.add(n, PersonGender, p.gender.toString)
    PersonIndex.add(n, PersonDateOfBirth, ValueContext.numeric(p.dateOfBirth.toDateTimeAtStartOfDay.getMillis))
    PersonIndex.add(n, PersonPlaceOfBirth, p.placeOfBirth)
  }

  private def index(n: Node, s: Soundtrack) {
    SoundtrackIndex.remove(n)
    SoundtrackIndex.add(n, SoundtrackLanguageCode, s.languageCode)
    SoundtrackIndex.add(n, SoundtrackFormatCode, s.formatCode)
  }

  private def index(n: Node, s: Subtitle) {
    SubtitleIndex.remove(n)
    SubtitleIndex.add(n, SubtitleLanguageCode, s.languageCode)
  }

  def exists: PartialFunction[VersionedLongIdEntity, Boolean] = {
    case ac: AbstractCast => exists(ac)
    case c: Character => exists(c)
    case dc: DigitalContainer => exists(dc)
    case m: MotionPicture => exists(m)
    case p: Person => exists(p)
    case s: Soundtrack => exists(s)
    case s: Subtitle => exists(s)
  }

  private def exists(ac: AbstractCast): Boolean = lookUpExact(ac).isDefined

  private def exists(c: Character): Boolean = lookUpExact(c).isDefined

  private def exists(dc: DigitalContainer): Boolean = lookUpExact(dc).isDefined

  private def exists(m: MotionPicture): Boolean = lookUpExact(m).isDefined

  private def exists(p: Person): Boolean = lookUpExact(p).isDefined

  private def exists(s: Soundtrack): Boolean = lookUpExact(s).isDefined

  private def exists(s: Subtitle): Boolean = lookUpExact(s).isDefined

  private def lookUpExact(ac: AbstractCast): Option[Node] = {
    val personNode = lookUpExact(ac.person)
    val characterNode = lookUpExact(ac.character)
    val motionPictureNode = lookUpExact(ac.motionPicture)
    if (!personNode.isDefined || !characterNode.isDefined || !motionPictureNode.isDefined) return None
    val castsWithSamePerson = CastIndex.get("person", personNode.get.getId).iterator.asScala.toSet
    val castsWithSameCharacter = CastIndex.get("character", characterNode.get.getId).iterator.asScala.toSet
    val castsWithSameMotionPicture = CastIndex.get("motionPicture", motionPictureNode.get.getId).iterator.asScala.toSet
    (castsWithSamePerson & castsWithSameCharacter & castsWithSameMotionPicture).headOption
  }

  private def lookUpExact(c: Character): Option[Node] = {
    val query = new BooleanQuery()
    query.add(new TermQuery(new Term(CharacterName, c.name)), MUST)
    query.add(new TermQuery(new Term(CharacterDiscriminator, c.discriminator)), MUST)
    Option(CharacterIndex.query(query).getSingle)
  }

  private def lookUpExact(dc: DigitalContainer): Option[Node] = {
    val motionPictureNode = lookUpExact(dc.motionPicture)
    val soundtrackNodes = (for (s <- dc.soundtracks) yield lookUpExact(s)).flatten
    val subtitleNodes = (for (s <- dc.subtitles) yield lookUpExact(s)).flatten
    if (!motionPictureNode.isDefined || soundtrackNodes.size != dc.soundtracks.size || subtitleNodes.size != dc.subtitles.size) return None
    val dcsWithSameMotionPicture = DigitalContainerIndex.get("motionPicture", motionPictureNode.get.getId).iterator.asScala.toSet
    val dcSetWithSameSoundtracks = for (s <- soundtrackNodes) yield DigitalContainerIndex.get("soundtrack", s.getId).iterator.asScala.toSet
    val dcSetWithSameSubtitles = for (s <- subtitleNodes) yield DigitalContainerIndex.get("subtitle", s.getId).iterator.asScala.toSet
    val dcsWithSameSoundtracks = if (dcSetWithSameSoundtracks.isEmpty) Set.empty[Node] else dcSetWithSameSoundtracks.reduce(_ & _)
    val dcsWithSameSubtitles = if (dcSetWithSameSubtitles.isEmpty) Set.empty[Node] else dcSetWithSameSubtitles.reduce(_ & _)
    (dcsWithSameMotionPicture & dcsWithSameSoundtracks & dcsWithSameSubtitles).filter((n: Node) => n.getRelationships(WithSoundtrack, OUTGOING).iterator.asScala.size == dc.soundtracks.size && n.getRelationships(WithSubtitle, OUTGOING).iterator().asScala.size == dc.subtitles.size).headOption
  }

  private def lookUpExact(m: MotionPicture): Option[Node] = {
    val releaseDateMillis = m.releaseDate.toDateTimeAtStartOfDay.getMillis
    val query = new BooleanQuery()
    query.add(new TermQuery(new Term(MovieOriginalTitle, m.originalTitle.text)), MUST)
    query.add(new TermQuery(new Term(MovieOriginalTitle + LocaleLanguage, m.originalTitle.locale.getLanguage)), MUST)
    query.add(new TermQuery(new Term(MovieOriginalTitle + LocaleCountry, m.originalTitle.locale.getCountry)), MUST)
    query.add(new TermQuery(new Term(MovieOriginalTitle + LocaleVariant, m.originalTitle.locale.getVariant)), MUST)
    query.add(NumericRangeQuery.newLongRange(MovieReleaseDate, releaseDateMillis, releaseDateMillis, true, true), MUST)
    Option(MotionPictureIndex.query(query).getSingle)
  }

  private def lookUpExact(p: Person): Option[Node] = {
    val dateOfBirthMillis = p.dateOfBirth.toDateTimeAtStartOfDay.getMillis
    val query = new BooleanQuery()
    query.add(new TermQuery(new Term(PersonName, p.name)), MUST)
    query.add(new TermQuery(new Term(PersonGender, p.gender.toString)), MUST)
    query.add(NumericRangeQuery.newLongRange(PersonDateOfBirth, dateOfBirthMillis, dateOfBirthMillis, true, true), MUST)
    query.add(new TermQuery(new Term(PersonPlaceOfBirth, p.placeOfBirth)), MUST)
    Option(PersonIndex.query(query).getSingle)
  }

  private def lookUpExact(s: Soundtrack): Option[Node] = {
    val query = new BooleanQuery()
    query.add(new TermQuery(new Term(SoundtrackLanguageCode, s.languageCode)), MUST)
    query.add(new TermQuery(new Term(SoundtrackFormatCode, s.formatCode)), MUST)
    Option(SoundtrackIndex.query(query).getSingle)
  }

  private def lookUpExact(s: Subtitle): Option[Node] = {
    val query = new BooleanQuery()
    query.add(new TermQuery(new Term(SubtitleLanguageCode, s.languageCode)), MUST)
    Option(SubtitleIndex.query(query).getSingle)
  }
}

private[utility] object IndexManager {

  private final val Instances = MutableMap.empty[GraphDatabaseService, IndexManager]

  def apply(db: GraphDatabaseService): IndexManager = {
    if (!Instances.contains(db)) {
      Instances += db -> new IndexManager(db)
    }
    Instances(db)
  }
}
