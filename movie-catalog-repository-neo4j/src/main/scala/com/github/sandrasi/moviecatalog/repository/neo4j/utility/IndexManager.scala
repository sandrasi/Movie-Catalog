package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MutableMap}
import org.neo4j.index.lucene.ValueContext
import org.neo4j.graphdb.{GraphDatabaseService, Node}
import org.neo4j.graphdb.Direction._
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain._
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.MovieCatalogDbConstants._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.DigitalContainerRelationshipType._
import org.apache.lucene.index.Term
import org.apache.lucene.search.{BooleanQuery, NumericRangeQuery, TermQuery, TermRangeQuery}
import org.apache.lucene.search.BooleanClause.Occur._

private[neo4j] class IndexManager(db: GraphDatabaseService) {

  Validate.notNull(db)

  private final val IdxMgr = db.index()
  private final val CastIndex = IdxMgr.forNodes("Cast")
  private final val CharacterIndex = IdxMgr.forNodes("Characters")
  private final val DigitalContainerIndex = IdxMgr.forNodes("DigitalContainers")
  private final val GenreIndex = IdxMgr.forNodes("Genres")
  private final val MotionPictureIndex = IdxMgr.forNodes("MotionPicture") // TODO (sandrasi): separate index for movies and tv shows
  private final val PersonIndex = IdxMgr.forNodes("Persons")
  private final val SoundtrackIndex = IdxMgr.forNodes("Soundtracks")
  private final val SubtitleIndex = IdxMgr.forNodes("Subtitles")

  def index: PartialFunction[(Node, Entity), Unit] = {
    case (n, c: Cast) => index(n, c)
    case (n, c: Character) => index(n, c)
    case (n, dc: DigitalContainer) => index(n, dc)
    case (n, g: Genre) => index(n, g)
    case (n, m: MotionPicture) => index(n, m)
    case (n, p: Person) => index(n, p)
    case (n, s: Soundtrack) => index(n, s)
    case (n, s: Subtitle) => index(n, s)
  }

  private def index(n: Node, c: Cast) {
    CastIndex.remove(n)
    CastIndex.add(n, CastPerson, lookUpExact(c.person).get.getId)
    CastIndex.add(n, CastCharacter, lookUpExact(c.character).get.getId)
    CastIndex.add(n, CastMotionPicture, lookUpExact(c.motionPicture).get.getId)
  }

  private def index(n: Node, c: Character) {
    CharacterIndex.remove(n)
    CharacterIndex.add(n, CharacterName, c.name)
    if (c.creator.isDefined) CharacterIndex.add(n, CharacterCreator, c.creator.get) else CharacterIndex.remove(n, CharacterCreator)
    if (c.creationDate.isDefined) CharacterIndex.add(n, CharacterCreationDate, ValueContext.numeric(c.creationDate.get.toDateTimeAtStartOfDay.getMillis)) else CharacterIndex.remove(n, CharacterCreationDate)
  }

  private def index(n: Node, dc: DigitalContainer) {
    DigitalContainerIndex.remove(n)
    DigitalContainerIndex.add(n, DigitalContainerMotionPicture, lookUpExact(dc.motionPicture).get.getId)
    dc.soundtracks.foreach((s: Soundtrack) => DigitalContainerIndex.add(n, DigitalContainerSoundtrack, lookUpExact(s).get.getId))
    dc.subtitles.foreach((s: Subtitle) => DigitalContainerIndex.add(n, DigitalContainerSubtitle, lookUpExact(s).get.getId))
  }

  private def index(n: Node, g: Genre) {
    GenreIndex.remove(n)
    GenreIndex.add(n, GenreCode, g.code)
  }

  private def index(n: Node, m: MotionPicture) {
    MotionPictureIndex.remove(n)
    MotionPictureIndex.add(n, MovieOriginalTitle, m.originalTitle.text)
    MotionPictureIndex.add(n, MovieOriginalTitle + LocaleLanguage, m.originalTitle.locale.getLanguage)
    MotionPictureIndex.add(n, MovieOriginalTitle + LocaleCountry, m.originalTitle.locale.getCountry)
    MotionPictureIndex.add(n, MovieOriginalTitle + LocaleVariant, m.originalTitle.locale.getVariant)
    if (m.releaseDate.isDefined) MotionPictureIndex.add(n, MovieReleaseDate, ValueContext.numeric(m.releaseDate.get.toDateTimeAtStartOfDay.getMillis)) else MotionPictureIndex.remove(n, MovieReleaseDate)
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

  def exists: PartialFunction[Entity, Boolean] = {
    case c: Cast => exists(c)
    case c: Character => exists(c)
    case dc: DigitalContainer => exists(dc)
    case g: Genre => exists(g)
    case m: MotionPicture => exists(m)
    case p: Person => exists(p)
    case s: Soundtrack => exists(s)
    case s: Subtitle => exists(s)
  }

  private def exists(c: Cast): Boolean = lookUpExact(c).isDefined

  private def exists(c: Character): Boolean = lookUpExact(c).isDefined

  private def exists(dc: DigitalContainer): Boolean = lookUpExact(dc).isDefined

  private def exists(g: Genre): Boolean = lookUpExact(g).isDefined

  private def exists(m: MotionPicture): Boolean = lookUpExact(m).isDefined

  private def exists(p: Person): Boolean = lookUpExact(p).isDefined

  private def exists(s: Soundtrack): Boolean = lookUpExact(s).isDefined

  private def exists(s: Subtitle): Boolean = lookUpExact(s).isDefined

  def lookUpExact: PartialFunction[Entity, Option[Node]] = {
    case c: Cast => lookUpExact(c)
    case c: Character => lookUpExact(c)
    case dc: DigitalContainer => lookUpExact(dc)
    case g: Genre => lookUpExact(g)
    case m: MotionPicture => lookUpExact(m)
    case p: Person => lookUpExact(p)
    case s: Soundtrack => lookUpExact(s)
    case s: Subtitle => lookUpExact(s)
  }

  private def lookUpExact(c: Cast): Option[Node] = {
    val personNode = lookUpExact(c.person)
    val characterNode = lookUpExact(c.character)
    val motionPictureNode = lookUpExact(c.motionPicture)
    if (personNode.isEmpty || characterNode.isEmpty || motionPictureNode.isEmpty) return None
    val castsWithSamePerson = CastIndex.get(CastPerson, personNode.get.getId).iterator.asScala.toSet
    val castsWithSameCharacter = CastIndex.get(CastCharacter, characterNode.get.getId).iterator.asScala.toSet
    val castsWithSameMotionPicture = CastIndex.get(CastMotionPicture, motionPictureNode.get.getId).iterator.asScala.toSet
    (castsWithSamePerson & castsWithSameCharacter & castsWithSameMotionPicture).headOption
  }

  private def lookUpExact(c: Character): Option[Node] = {
    val query = new BooleanQuery()
    query.add(new TermQuery(new Term(CharacterName, c.name)), MUST)
    if (c.creator.isDefined) query.add(new TermQuery(new Term(CharacterCreator, c.creator.get)), MUST)
    else query.add(new TermRangeQuery(CharacterCreator, Char.MinValue.toString, Char.MaxValue.toString, true, true), MUST_NOT)
    if (c.creationDate.isDefined) {
      val cdm = c.creationDate.get.toDateTimeAtStartOfDay.getMillis
      query.add(NumericRangeQuery.newLongRange(CharacterCreationDate, cdm, cdm, true, true), MUST)
    } else query.add(NumericRangeQuery.newLongRange(CharacterCreationDate, Long.MinValue, Long.MaxValue, true, true), MUST_NOT)
    Option(CharacterIndex.query(query).getSingle)
  }

  private def lookUpExact(dc: DigitalContainer): Option[Node] = {
    val motionPictureNode = lookUpExact(dc.motionPicture)
    val soundtrackNodes = (for (s <- dc.soundtracks) yield lookUpExact(s)).flatten
    val subtitleNodes = (for (s <- dc.subtitles) yield lookUpExact(s)).flatten
    if (motionPictureNode.isEmpty || soundtrackNodes.size != dc.soundtracks.size || subtitleNodes.size != dc.subtitles.size) return None
    val dcsWithSameMotionPicture = DigitalContainerIndex.get(DigitalContainerMotionPicture, motionPictureNode.get.getId).iterator.asScala.toSet
    val dcSetWithSameSoundtracks = for (s <- soundtrackNodes) yield DigitalContainerIndex.get(DigitalContainerSoundtrack, s.getId).iterator.asScala.toSet
    val dcSetWithSameSubtitles = for (s <- subtitleNodes) yield DigitalContainerIndex.get(DigitalContainerSubtitle, s.getId).iterator.asScala.toSet
    val dcsWithSameSoundtracks = if (dcSetWithSameSoundtracks.isEmpty) Set.empty[Node] else dcSetWithSameSoundtracks.reduce(_ & _)
    val dcsWithSameSubtitles = if (dcSetWithSameSubtitles.isEmpty) Set.empty[Node] else dcSetWithSameSubtitles.reduce(_ & _)
    (dcsWithSameMotionPicture & dcsWithSameSoundtracks & dcsWithSameSubtitles).filter((n: Node) => n.getRelationships(WithSoundtrack, OUTGOING).iterator.asScala.size == dc.soundtracks.size && n.getRelationships(WithSubtitle, OUTGOING).iterator().asScala.size == dc.subtitles.size).headOption
  }

  private def lookUpExact(g: Genre): Option[Node] = {
    val query = new BooleanQuery()
    query.add(new TermQuery(new Term(GenreCode, g.code)), MUST)
    Option(GenreIndex.query(query).getSingle)
  }

  private def lookUpExact(m: MotionPicture): Option[Node] = {
    val query = new BooleanQuery()
    query.add(new TermQuery(new Term(MovieOriginalTitle, m.originalTitle.text)), MUST)
    query.add(new TermQuery(new Term(MovieOriginalTitle + LocaleLanguage, m.originalTitle.locale.getLanguage)), MUST)
    query.add(new TermQuery(new Term(MovieOriginalTitle + LocaleCountry, m.originalTitle.locale.getCountry)), MUST)
    query.add(new TermQuery(new Term(MovieOriginalTitle + LocaleVariant, m.originalTitle.locale.getVariant)), MUST)
    if (m.releaseDate.isDefined) {
      val rdm = m.releaseDate.get.toDateTimeAtStartOfDay.getMillis
      query.add(NumericRangeQuery.newLongRange(MovieReleaseDate, rdm, rdm, true, true), MUST)
    } else query.add(NumericRangeQuery.newLongRange(MovieReleaseDate, Long.MinValue, Long.MaxValue, true, true), MUST_NOT)
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

private[neo4j] object IndexManager {

  private final val Instances = MutableMap.empty[GraphDatabaseService, IndexManager]

  def apply(db: GraphDatabaseService): IndexManager = {
    if (!Instances.contains(db)) {
      Instances += db -> new IndexManager(db)
    }
    Instances(db)
  }
}
