package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.JavaConversions._
import scala.collection.mutable.{Map => MutableMap}
import java.util.Locale
import java.util.Locale.US
import org.neo4j.graphdb._
import org.neo4j.graphdb.Direction._
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.LongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.container._
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}
import com.github.sandrasi.moviecatalog.domain.utility.Gender
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.CharacterRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.DigitalContainerRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.FilmCrewRelationshipType
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.PropertyManager._
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.{AbstractCast, Actor, Actress}

private[neo4j] class EntityFactory private (db: GraphDatabaseService) extends MovieCatalogGraphPropertyNames {

  Validate.notNull(db)

  private final val SubrefNodeSupp = SubreferenceNodeSupport(db)

  private final val ClassActor = classOf[Actor]
  private final val ClassActress = classOf[Actress]
  private final val ClassCharacter = classOf[Character]
  private final val ClassDigitalContainer = classOf[DigitalContainer]
  private final val ClassMovie = classOf[Movie]
  private final val ClassPerson = classOf[Person]
  private final val ClassSoundtrack = classOf[Soundtrack]
  private final val ClassSubtitle = classOf[Subtitle]

  def createEntityFrom[A <: LongIdEntity](n: Node, entityType: Class[A])(implicit locale: Locale = US): A = withTypeCheck(n, entityType ) {
    entityType match {
      case ClassActor => createActorFrom(n)
      case ClassActress => createActressFrom(n)
      case ClassCharacter => createCharacterFrom(n)
      case ClassDigitalContainer => createDigitalContainerFrom(n, locale)
      case ClassMovie => createMovieFrom(n)
      case ClassPerson => createPersonFrom(n)
      case ClassSoundtrack => createSoundtrackFrom(n, locale)
      case ClassSubtitle => createSubtitleFrom(n, locale)
      case _ => throw new IllegalArgumentException("Unsupported entity type: %s".format(entityType.getName))
    }
  }
  
  private def withTypeCheck[A <: LongIdEntity](n: Node, entityType: Class[A])(op: => LongIdEntity) = if (SubrefNodeSupp.isNodeOfType(n, entityType)) entityType.cast(op) else throw new ClassCastException("Node [id: %d] is not of type %s".format(n.getId, entityType.getName))

  private def createActorFrom(n: Node) = Actor(createPersonFrom(n.getSingleRelationship(FilmCrewRelationshipType.forClass(classOf[Actor]), OUTGOING).getEndNode), createCharacterFrom(n.getSingleRelationship(PlayedBy, OUTGOING).getEndNode), createMovieFrom(n.getSingleRelationship(AppearedIn, OUTGOING).getEndNode), getLong(n, Version), n.getId)

  private def createActressFrom(n: Node) = Actress(createPersonFrom(n.getSingleRelationship(FilmCrewRelationshipType.forClass(classOf[Actress]), OUTGOING).getEndNode), createCharacterFrom(n.getSingleRelationship(PlayedBy, OUTGOING).getEndNode), createMovieFrom(n.getSingleRelationship(AppearedIn, OUTGOING).getEndNode), getLong(n, Version), n.getId)

  private def createCharacterFrom(n: Node) = Character(getString(n, CharacterName), getString(n, CharacterDiscriminator), getLong(n, Version), n.getId)

  private def createDigitalContainerFrom(n: Node, l: Locale) = DigitalContainer(createMovieFrom(n.getSingleRelationship(StoredIn, INCOMING).getStartNode), getSoundtracks(n, l), getSubtitles(n, l), n.getId)
  
  private def getSoundtracks(n: Node, l: Locale) = n.getRelationships(WithSoundtrack, OUTGOING).map(r => createSoundtrackFrom(r.getEndNode, l)).toSet

  private def getSubtitles(n: Node, l: Locale) = n.getRelationships(WithSubtitle, OUTGOING).map(r => createSubtitleFrom(r.getEndNode, l)).toSet

  private def createMovieFrom(n: Node) = Movie(getLocalizedText(n, MovieOriginalTitle), getLocalizedTextSet(n, MovieLocalizedTitles), getDuration(n, MovieLength), getLocalDate(n, MovieReleaseDate), n.getId)

  private def createPersonFrom(n: Node) = Person(getString(n, PersonName), Gender.withName(getString(n, PersonGender)), getLocalDate(n, PersonDateOfBirth), getString(n, PersonPlaceOfBirth), n.getId)

  private def createSoundtrackFrom(n: Node, l: Locale) = Soundtrack(getString(n, SoundtrackLanguageCode), getString(n, SoundtrackFormatCode), getSoundtrackLanguageName(n, l), getSoundtrackFormatName(n, l), n.getId)

  private def getSoundtrackLanguageName(n: Node, l: Locale) = try { getLocalizedText(n, SoundtrackLanguageNames, l) } catch { case _: NotFoundException | _: NoSuchElementException=> null }
  
  private def getSoundtrackFormatName(n: Node, l: Locale) = try { getLocalizedText(n, SoundtrackFormatNames, l) } catch { case _: NotFoundException | _: NoSuchElementException => null }

  private def createSubtitleFrom(n: Node, l: Locale) = Subtitle(getString(n, SubtitleLanguageCode), getSubtitleLanguageName(n, l), n.getId)

  private def getSubtitleLanguageName(n: Node, l: Locale) = try { getLocalizedText(n, SubtitleLanguageNames, l) } catch { case _: NotFoundException | _: NoSuchElementException => null }
}

private[neo4j] object EntityFactory {
  
  private final val Instances = MutableMap.empty[GraphDatabaseService, EntityFactory]

  def apply(db: GraphDatabaseService): EntityFactory = {
    if (!Instances.contains(db)) {
      Instances += db -> new EntityFactory(db)
    }
    Instances(db)
  }
}
