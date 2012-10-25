package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MutableMap}
import java.util.Locale
import java.util.Locale.US
import org.neo4j.graphdb._
import org.neo4j.graphdb.Direction.OUTGOING
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.{Actor, Actress}
import com.github.sandrasi.moviecatalog.domain.entities.container._
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}
import com.github.sandrasi.moviecatalog.domain.utility.Gender
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.CharacterRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.DigitalContainerRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.FilmCrewRelationshipType
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.MovieCatalogDbConstants._
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.PropertyManager._

private[neo4j] class EntityFactory private (db: GraphDatabaseService) {

  Validate.notNull(db)

  private final val SubrefNodeSupp = SubreferenceNodeSupport(db)

  def createEntityFrom[A <: VersionedLongIdEntity](n: Node, entityType: Class[A])(implicit locale: Locale = US): A = withTypeCheck(n, entityType) {
    entityType match {
      case ClassAbstractCast => createAbstractCastFrom(n)
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
  
  private def withTypeCheck[A <: VersionedLongIdEntity](n: Node, entityType: Class[A])(op: => VersionedLongIdEntity) = if (SubrefNodeSupp.isNodeOfType(n, entityType)) entityType.cast(op) else throw new ClassCastException("Node [id: %d] is not of type %s".format(n.getId, entityType.getName))
  
  private def createAbstractCastFrom(n: Node) = if (SubrefNodeSupp.isNodeOfType(n, classOf[Actor])) createActorFrom(n)
    else if (SubrefNodeSupp.isNodeOfType(n, classOf[Actress])) createActressFrom(n)
    else throw new IllegalArgumentException("%s cannot be instantiated from node [%d]".format(ClassAbstractCast.getName, n.getId))

  private def createActorFrom(n: Node) = Actor(createPersonFrom(n.getSingleRelationship(FilmCrewRelationshipType.forClass(classOf[Actor]), OUTGOING).getEndNode), createCharacterFrom(n.getSingleRelationship(Played, OUTGOING).getEndNode), createMovieFrom(n.getSingleRelationship(AppearedIn, OUTGOING).getEndNode), getLong(n, Version), n.getId)

  private def createActressFrom(n: Node) = Actress(createPersonFrom(n.getSingleRelationship(FilmCrewRelationshipType.forClass(classOf[Actress]), OUTGOING).getEndNode), createCharacterFrom(n.getSingleRelationship(Played, OUTGOING).getEndNode), createMovieFrom(n.getSingleRelationship(AppearedIn, OUTGOING).getEndNode), getLong(n, Version), n.getId)

  private def createCharacterFrom(n: Node) = new Character(getString(n, CharacterName), getString(n, CharacterCreator), getLocalDate(n, CharacterCreationDate), getLong(n, Version), n.getId)

  private def createDigitalContainerFrom(n: Node, l: Locale) = DigitalContainer(createMovieFrom(n.getSingleRelationship(WithContent, OUTGOING).getEndNode), getSoundtracks(n, l), getSubtitles(n, l), getLong(n, Version), n.getId)
  
  private def getSoundtracks(n: Node, l: Locale) = n.getRelationships(WithSoundtrack, OUTGOING).asScala.map(r => createSoundtrackFrom(r.getEndNode, l)).toSet

  private def getSubtitles(n: Node, l: Locale) = n.getRelationships(WithSubtitle, OUTGOING).asScala.map(r => createSubtitleFrom(r.getEndNode, l)).toSet

  private def createMovieFrom(n: Node) = Movie(getLocalizedText(n, MovieOriginalTitle), getLocalizedTextSet(n, MovieLocalizedTitles), getDuration(n, MovieRuntime), getLocalDate(n, MovieReleaseDate), getLong(n, Version), n.getId)

  private def createPersonFrom(n: Node) = Person(getString(n, PersonName), Gender.withName(getString(n, PersonGender)), getLocalDate(n, PersonDateOfBirth), getString(n, PersonPlaceOfBirth), getLong(n, Version), n.getId)

  private def createSoundtrackFrom(n: Node, l: Locale) = Soundtrack(getString(n, SoundtrackLanguageCode), getString(n, SoundtrackFormatCode), getSoundtrackLanguageName(n, l), getSoundtrackFormatName(n, l), getLong(n, Version), n.getId)

  private def getSoundtrackLanguageName(n: Node, l: Locale) = try { getLocalizedText(n, SoundtrackLanguageNames, l) } catch { case _: NotFoundException | _: NoSuchElementException => null }
  
  private def getSoundtrackFormatName(n: Node, l: Locale) = try { getLocalizedText(n, SoundtrackFormatNames, l) } catch { case _: NotFoundException | _: NoSuchElementException => null }

  private def createSubtitleFrom(n: Node, l: Locale) = Subtitle(getString(n, SubtitleLanguageCode), getSubtitleLanguageName(n, l), getLong(n, Version), n.getId)

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
