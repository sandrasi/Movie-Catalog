package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain._
import com.github.sandrasi.moviecatalog.domain.utility.Gender
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.CharacterRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.CrewRelationshipType
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.DigitalContainerRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.MotionPictureRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.MovieCatalogDbConstants._
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.PropertyManager._
import java.util.Locale
import java.util.Locale.US
import org.neo4j.graphdb.{GraphDatabaseService, Node, NotFoundException}
import org.neo4j.graphdb.Direction.OUTGOING
import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MutableMap}

private[neo4j] class EntityFactory private (db: GraphDatabaseService) {

  Validate.notNull(db)

  private final val DbMgr = DatabaseManager(db)

  def createEntityFrom[A <: Entity](n: Node, entityType: Class[A])(implicit locale: Locale = US): A = withTypeCheck(n, entityType) {
    entityType match {
      case ClassCast => createAbstractCastFrom(n, locale)
      case ClassActor => createActorFrom(n, locale)
      case ClassActress => createActressFrom(n, locale)
      case ClassCharacter => createCharacterFrom(n)
      case ClassDigitalContainer => createDigitalContainerFrom(n, locale)
      case ClassGenre => createGenreFrom(n, locale)
      case ClassMovie => createMovieFrom(n, locale)
      case ClassPerson => createPersonFrom(n)
      case ClassSoundtrack => createSoundtrackFrom(n, locale)
      case ClassSubtitle => createSubtitleFrom(n, locale)
      case _ => throw new IllegalArgumentException("Unsupported entity type: %s".format(entityType.getName))
    }
  }
  
  private def withTypeCheck[A <: Entity](n: Node, entityType: Class[A])(op: => Entity) = if (DbMgr.isNodeOfType(n, entityType)) entityType.cast(op) else throw new ClassCastException("Node [id: %d] is not of type %s".format(n.getId, entityType.getName))
  
  private def createAbstractCastFrom(n: Node, l: Locale) = if (DbMgr.isNodeOfType(n, classOf[Actor])) createActorFrom(n, l)
    else if (DbMgr.isNodeOfType(n, classOf[Actress])) createActressFrom(n, l)
    else throw new IllegalArgumentException("%s cannot be instantiated from node [%d]".format(ClassCast.getName, n.getId))

  private def createActorFrom(n: Node, l: Locale) = Actor(createPersonFrom(n.getSingleRelationship(CrewRelationshipType.forClass(classOf[Actor]), OUTGOING).getEndNode), createCharacterFrom(n.getSingleRelationship(Played, OUTGOING).getEndNode), createMovieFrom(n.getSingleRelationship(AppearedIn, OUTGOING).getEndNode, l), getLong(n, Version), getUuid(n))

  private def createActressFrom(n: Node, l: Locale) = Actress(createPersonFrom(n.getSingleRelationship(CrewRelationshipType.forClass(classOf[Actress]), OUTGOING).getEndNode), createCharacterFrom(n.getSingleRelationship(Played, OUTGOING).getEndNode), createMovieFrom(n.getSingleRelationship(AppearedIn, OUTGOING).getEndNode, l), getLong(n, Version), getUuid(n))

  private def createCharacterFrom(n: Node) = Character(getString(n, CharacterName).get, getString(n, CharacterCreator), getLocalDate(n, CharacterCreationDate), getLong(n, Version), Some(getUuid(n)))

  private def createDigitalContainerFrom(n: Node, l: Locale) = DigitalContainer(createMovieFrom(n.getSingleRelationship(WithContent, OUTGOING).getEndNode, l), getSoundtracks(n, l), getSubtitles(n, l), getLong(n, Version), getUuid(n))
  
  private def getSoundtracks(n: Node, l: Locale) = n.getRelationships(WithSoundtrack, OUTGOING).asScala.map(r => createSoundtrackFrom(r.getEndNode, l)).toSet

  private def getSubtitles(n: Node, l: Locale) = n.getRelationships(WithSubtitle, OUTGOING).asScala.map(r => createSubtitleFrom(r.getEndNode, l)).toSet

  private def createGenreFrom(n: Node, l: Locale) = Genre(getString(n, GenreCode).get, getLocalizedText(n, GenreName, l), getLong(n, Version), Some(getUuid(n)))

  private def createMovieFrom(n: Node, l: Locale) = Movie(getLocalizedText(n, MovieOriginalTitle).get, getLocalizedText(n, MovieLocalizedTitle, l), getGenres(n, l), getDuration(n, MovieRuntime), getLocalDate(n, MovieReleaseDate), getLong(n, Version), Some(getUuid(n)))

  private def getGenres(n: Node, l: Locale) = n.getRelationships(HasGenre, OUTGOING).asScala.map(r => createGenreFrom(r.getEndNode, l)).toSet

  private def createPersonFrom(n: Node) = Person(getString(n, PersonName).get, Gender.valueOf(getString(n, PersonGender).get), getLocalDate(n, PersonDateOfBirth).get, getString(n, PersonPlaceOfBirth).get, getLong(n, Version), getUuid(n))

  private def createSoundtrackFrom(n: Node, l: Locale) = Soundtrack(getString(n, SoundtrackLanguageCode).get, getString(n, SoundtrackFormatCode).get, getSoundtrackLanguageName(n, l), getSoundtrackFormatName(n, l), getLong(n, Version), getUuid(n))

  private def getSoundtrackLanguageName(n: Node, l: Locale) = try { getLocalizedText(n, SoundtrackLanguageName, l).get } catch { case _: NotFoundException | _: NoSuchElementException => null }
  
  private def getSoundtrackFormatName(n: Node, l: Locale) = try { getLocalizedText(n, SoundtrackFormatName, l).get } catch { case _: NotFoundException | _: NoSuchElementException => null }

  private def createSubtitleFrom(n: Node, l: Locale) = Subtitle(getString(n, SubtitleLanguageCode).get, getSubtitleLanguageName(n, l), getLong(n, Version), getUuid(n))

  private def getSubtitleLanguageName(n: Node, l: Locale) = try { getLocalizedText(n, SubtitleLanguageName, l).get } catch { case _: NotFoundException | _: NoSuchElementException => null }
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
