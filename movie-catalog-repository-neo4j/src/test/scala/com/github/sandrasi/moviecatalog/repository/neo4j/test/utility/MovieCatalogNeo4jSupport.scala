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
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.{Actor, Actress}
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
  protected final val TestMovie = Movie("Test movie title", Set(LocalizedText("Teszt film cím")(HungarianLocale), LocalizedText("Prova film titolo")(ItalianLocale)), Duration.standardMinutes(90), new LocalDate(2011, 1, 1))
  protected final val JohnDoe = Person("John Doe", Male, new LocalDate(1980, 8, 8), "Anytown")
  protected final val JaneDoe = Person("Jane Doe", Female, new LocalDate(1990, 9, 9), "Anyville")
  protected final val EnglishSoundtrack = Soundtrack("en", "dts", "English", "DTS")
  protected final val HungarianSoundtrack = Soundtrack("hu", "dts", "Hungarian", "DTS")
  protected final val EnglishSubtitle = Subtitle("en", "English")
  protected final val HungarianSubtitle = Subtitle("hu", "Hungarian")

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

  protected def createNodeFrom(e: LongIdEntity): Node = transaction(db) { NodeManager(db).createNodeFrom(e) }
  
  protected def updateNodeOf(e: LongIdEntity, l: Locale = AmericanLocale): Node = transaction(db) { NodeManager(db).updateNodeOf(e)(l) }

  protected def createRelationship(from: Node, to: Node, relType: RelationshipType): Relationship = transaction(db) { db.createNode().createRelationshipTo(db.createNode(), relType) }

  protected def insertEntity[A <: LongIdEntity](entity: A): A = entity match {
    case a: Actor => createActorFrom(createNodeFrom(a)).asInstanceOf[A]
    case a: Actress => createActressFrom(createNodeFrom(a)).asInstanceOf[A]
    case c: Character => createCharacterFrom(createNodeFrom(c)).asInstanceOf[A]
    case dc: DigitalContainer => createDigitalContainerFrom(createNodeFrom(dc)).asInstanceOf[A]
    case m: Movie => createMovieFrom(createNodeFrom(m)).asInstanceOf[A]
    case p: Person => createPersonFrom(createNodeFrom(p)).asInstanceOf[A]
    case s: Soundtrack => createSoundtrackFrom(createNodeFrom(s)).asInstanceOf[A]
    case s: Subtitle => createSubtitleFrom(createNodeFrom(s)).asInstanceOf[A]
    case _ => throw new IllegalArgumentException("Unsupported entity type: %s".format(entity.getClass.getName))
  }

  protected def createActorFrom(n: Node): Actor = EntityFactory(db).createEntityFrom(n, classOf[Actor])

  protected def createActressFrom(n: Node): Actress = EntityFactory(db).createEntityFrom(n, classOf[Actress])

  protected def createCharacterFrom(n: Node): Character = EntityFactory(db).createEntityFrom(n, classOf[Character])

  protected def createDigitalContainerFrom(n: Node): DigitalContainer = EntityFactory(db).createEntityFrom(n, classOf[DigitalContainer])

  protected def createMovieFrom(n: Node): Movie = EntityFactory(db).createEntityFrom(n, classOf[Movie])

  protected def createPersonFrom(n: Node): Person = EntityFactory(db).createEntityFrom(n, classOf[Person])

  protected def createSoundtrackFrom(n: Node): Soundtrack = EntityFactory(db).createEntityFrom(n, classOf[Soundtrack])

  protected def createSubtitleFrom(n: Node): Subtitle = EntityFactory(db).createEntityFrom(n, classOf[Subtitle])

  protected def getNodeCount: Int = db.getAllNodes.iterator().size

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
