package com.github.sandrasi.moviecatalog.repository.neo4j.test.utility

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import java.io.IOException
import java.nio.file.FileVisitResult._
import java.nio.file.{Files, Path, Paths, SimpleFileVisitor}
import java.nio.file.attribute.BasicFileAttributes
import java.util.{Locale, UUID}
import org.joda.time.{Duration, LocalDate}
import org.neo4j.graphdb.{Node, Relationship, RelationshipType}
import org.neo4j.kernel.EmbeddedGraphDatabase
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll}
import com.github.sandrasi.moviecatalog.domain.entities.base.LongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText
import com.github.sandrasi.moviecatalog.domain.entities.container._
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}
import com.github.sandrasi.moviecatalog.domain.utility.Gender._
import com.github.sandrasi.moviecatalog.repository.neo4j.transaction.TransactionSupport
import com.github.sandrasi.moviecatalog.repository.neo4j.utility._

private[neo4j] trait MovieCatalogNeo4jSupport extends MovieCatalogGraphPropertyNames with TransactionSupport {

  self: BeforeAndAfterAll with BeforeAndAfterEach =>

  protected final val AmericanLocale = Locale.US
  protected final val HungarianLocale = new Locale("hu", "HU")
  protected final val ItalianLocale = Locale.ITALY
  protected final val Johnny = Character("Johnny")
  protected final val Jenny = Character("Jenny")
  protected final val TestMovie = Movie(LocalizedText("Test movie title"), Set(LocalizedText("Teszt film cím", HungarianLocale), LocalizedText("Prova film titolo", ItalianLocale)), Duration.standardMinutes(90), new LocalDate(2011, 1, 1))
  protected final val JohnDoe = Person("John Doe", Male, new LocalDate(1980, 8, 8), "Anytown")
  protected final val JaneDoe = Person("Jane Doe", Female, new LocalDate(1980, 8, 8), "Anyville")
  protected final val EnglishSoundtrack = Soundtrack("en", "dts", Some(LocalizedText("English")), Some(LocalizedText("DTS")))
  protected final val HungarianSoundtrack = Soundtrack("hu", "dts", Some(LocalizedText("Hungarian")), Some(LocalizedText("DTS")))
  protected final val EnglishSubtitle = Subtitle("en", Some(LocalizedText("English")))
  protected final val HungarianSubtitle = Subtitle("hu", Some(LocalizedText("Hungarian")))

  protected var db: EmbeddedGraphDatabase = _
  protected var subrefNodeSupp: SubreferenceNodeSupport = _

  private var dbs: ArrayBuffer[Database] = _

  override protected def beforeAll() {
    dbs = ArrayBuffer(Database.createTemporary("movie-catalog-repository-neo4j-test"))
    db = dbs(0).gDb
    subrefNodeSupp = SubreferenceNodeSupport(db)
  }

  override protected def afterAll() {
    dbs.foreach(cleanUpDatabase(_))
  }

  private def cleanUpDatabase(database: Database) {
    database.gDb.shutdown()
    if (!database.permanent) deleteStoreDir(database.gDb.getStoreDir)
  }

  private def deleteStoreDir(storeDir: String) {
    Files.walkFileTree(Paths.get(storeDir),
      new SimpleFileVisitor[Path] {

        override def visitFile(file: Path, attrs: BasicFileAttributes) = { Files.delete(file); CONTINUE }

        override def postVisitDirectory(dir: Path, e: IOException) = if (e == null) { Files.delete(dir); CONTINUE } else throw e
      })
  }

  protected override def afterEach() {
    for (db <- dbs) if (!db.permanent) truncateDb(db.gDb)
  }

  private def truncateDb(db: EmbeddedGraphDatabase) {
    transaction(db) { db.getAllNodes.view.filter(isDeletable(_, db)).foreach(deleteNode(_)) }
  }

  private def isDeletable(n: Node, db: EmbeddedGraphDatabase) = n != db.getReferenceNode

  protected def deleteNode(n: Node) {
    n.getRelationships.foreach(_.delete())
    n.delete()
  }

  protected def createTempDb(): EmbeddedGraphDatabase = {
    val db = Database.createTemporary()
    dbs += db
    db.gDb
  }

  protected def createNode(): Node = transaction(db) { db.createNode() }

  protected def createNode(e: LongIdEntity): Node = transaction(db) { NodeFactory(db).createNodeFrom(e) }

  protected def createRelationship(from: Node, to: Node, relType: RelationshipType): Relationship = transaction(db) { db.createNode().createRelationshipTo(db.createNode(), relType) }

  protected def createRelationship(e: LongIdEntity): Relationship = transaction(db) { RelationshipFactory(db).createRelationshipFrom(e) }

  protected def saveEntity[A <: LongIdEntity](entity: A): A = entity match {
    case c: Character => createCharacterEntity(createNode(c)).asInstanceOf[A]
    case m: Movie => createMovieEntity(createNode(m)).asInstanceOf[A]
    case p: Person => createPersonEntity(createNode(p)).asInstanceOf[A]
    case s: Soundtrack => createSoundtrackEntity(createNode(s)).asInstanceOf[A]
    case s: Subtitle => createSubtitleEntity(createNode(s)).asInstanceOf[A]
    case _ => throw new IllegalArgumentException("Unsupported entity type: %s".format(entity.getClass.getName))
  }

  protected def createCharacterEntity(n: Node): Character = EntityFactory(db).createEntityFrom(n, classOf[Character])

  protected def createMovieEntity(n: Node): Movie = EntityFactory(db).createEntityFrom(n, classOf[Movie])

  protected def createPersonEntity(n: Node): Person = EntityFactory(db).createEntityFrom(n, classOf[Person])

  protected def createSoundtrackEntity(n: Node): Soundtrack = EntityFactory(db).createEntityFrom(n, classOf[Soundtrack])

  protected def createSubtitleEntity(n: Node): Subtitle = EntityFactory(db).createEntityFrom(n, classOf[Subtitle])

  protected final class TestRelationshipType(override val name: String) extends RelationshipType
}

private final class Database private (val gDb: EmbeddedGraphDatabase, val permanent: Boolean)

private object Database {

  def createTemporary(): Database = createTemporary(UUID.randomUUID().toString)

  def createTemporary(tempStoreDirPrefix: String): Database = {
    val storeDir = Files.createTempDirectory(tempStoreDirPrefix)
    new Database(new EmbeddedGraphDatabase(storeDir.toString), false)
  }
}
