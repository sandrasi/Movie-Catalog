package com.github.sandrasi.moviecatalog.repository.neo4j
package test.utility

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import java.io.IOException
import java.nio.file.FileVisitResult._
import java.nio.file.{Files, Path, SimpleFileVisitor}
import java.nio.file.attribute.BasicFileAttributes
import java.util.{Locale, UUID}
import org.joda.time.{Duration, LocalDate}
import org.neo4j.graphdb.{GraphDatabaseService, Node, Relationship, RelationshipType}
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll}
import com.github.sandrasi.moviecatalog.common.LocalizedText
import com.github.sandrasi.moviecatalog.domain._
import com.github.sandrasi.moviecatalog.domain.utility.Gender._
import com.github.sandrasi.moviecatalog.repository.neo4j.transaction.TransactionSupport
import com.github.sandrasi.moviecatalog.repository.neo4j.utility._
import org.neo4j.test.TestGraphDatabaseFactory
import org.neo4j.tooling.GlobalGraphOperations

private[neo4j] trait MovieCatalogNeo4jSupport extends TransactionSupport {

  self: BeforeAndAfterAll with BeforeAndAfterEach =>

  protected final val AmericanLocale = Locale.US
  protected final val HungarianLocale = new Locale("hu", "HU")
  protected final val ItalianLocale = Locale.ITALY
  protected final val VincentVega = Character("Vincent Vega", "Quentin Tarantino", new LocalDate(1994, 10, 14))
  protected final val MiaWallace = Character("Mia Wallace", "Quentin Tarantino", new LocalDate(1994, 10, 14))
  protected final val Crime = Genre("crime", "Crime")
  protected final val Thriller = Genre("thriller", "Thriller")
  protected final val PulpFiction = Movie("Pulp fiction", LocalizedText("Ponyvaregény")(HungarianLocale), Set(), Duration.standardMinutes(154), new LocalDate(1994, 10, 14)) // TODO (sandrasi): set the genre when it's automatically saved with the movie
  protected final val JohnTravolta = Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.")
  protected final val UmaThurman = Person("Uma Karuna Thurman", Female, new LocalDate(1970, 4, 29), "Boston, Massachusetts, U.S.")
  protected final val EnglishSoundtrack = Soundtrack("en", "dts", "English", "DTS")
  protected final val HungarianSoundtrack = Soundtrack("hu", "dts", "Hungarian", "DTS")
  protected final val ItalianSoundtrack = Soundtrack("it", "dts", "Italian", "DTS")
  protected final val EnglishSubtitle = Subtitle("en", "English")
  protected final val HungarianSubtitle = Subtitle("hu", "Hungarian")
  protected final val ItalianSubtitle = Subtitle("it", "Italian")

  protected var db: GraphDatabaseService = _
  protected var dbMgr: DatabaseManager = _

  private var dbs: ArrayBuffer[Database] = _

  override protected def beforeAll() {
    dbs = ArrayBuffer(Database("movie-catalog-repository-neo4j-test"))
    db = dbs(0).db
    dbMgr = DatabaseManager(db)
  }

  override protected def afterAll() {
    dbs.foreach(_.shutDown())
  }

  protected override def afterEach() {
    dbs.foreach(db => if (!db.permanent) db.truncate())
  }

  protected def createTempDb(): GraphDatabaseService = {
    val db = Database()
    dbs += db
    db.db
  }

  protected def getNode(e: Entity): Node = dbMgr.getNodeOf(e)

  protected def createNode(): Node = transaction(db) { db.createNode() }

  protected def createNodeFrom(e: Entity): Node = {
    implicit val tx = db.beginTx()
    transaction(tx) { NodeManager(db).createNodeFrom(e) }
  }

  protected def updateNodeOf(e: Entity, l: Locale = AmericanLocale): Node = {
    val tx = db.beginTx()
    transaction(tx) { NodeManager(db).updateNodeOf(e)(tx = tx, l = l) }
  }

  protected def insertEntity[A <: Entity]: PartialFunction[A, A] = {
    case a: Actor => createActorFrom(createNodeFrom(a)).asInstanceOf[A]
    case a: Actress => createActressFrom(createNodeFrom(a)).asInstanceOf[A]
    case c: Character => createCharacterFrom(createNodeFrom(c)).asInstanceOf[A]
    case dc: DigitalContainer => createDigitalContainerFrom(createNodeFrom(dc)).asInstanceOf[A]
    case g: Genre => createGenreFrom(createNodeFrom(g)).asInstanceOf[A]
    case m: Movie => createMovieFrom(createNodeFrom(m)).asInstanceOf[A]
    case p: Person => createPersonFrom(createNodeFrom(p)).asInstanceOf[A]
    case s: Soundtrack => createSoundtrackFrom(createNodeFrom(s)).asInstanceOf[A]
    case s: Subtitle => createSubtitleFrom(createNodeFrom(s)).asInstanceOf[A]
  }

  protected def createActorFrom(n: Node): Actor = EntityFactory(db).createEntityFrom(n, classOf[Actor])

  protected def createActressFrom(n: Node): Actress = EntityFactory(db).createEntityFrom(n, classOf[Actress])

  protected def createCharacterFrom(n: Node): Character = EntityFactory(db).createEntityFrom(n, classOf[Character])

  protected def createDigitalContainerFrom(n: Node): DigitalContainer = EntityFactory(db).createEntityFrom(n, classOf[DigitalContainer])

  protected def createGenreFrom(n: Node): Genre = EntityFactory(db).createEntityFrom(n, classOf[Genre])

  protected def createMovieFrom(n: Node): Movie = EntityFactory(db).createEntityFrom(n, classOf[Movie])

  protected def createPersonFrom(n: Node): Person = EntityFactory(db).createEntityFrom(n, classOf[Person])

  protected def createSoundtrackFrom(n: Node): Soundtrack = EntityFactory(db).createEntityFrom(n, classOf[Soundtrack])

  protected def createSubtitleFrom(n: Node): Subtitle = EntityFactory(db).createEntityFrom(n, classOf[Subtitle])

  protected def getNodeCount: Int = GlobalGraphOperations.at(db).getAllNodes.iterator().asScala.size

  protected final class TestRelationshipType(override val name: String) extends RelationshipType
}

private final class Database private (val db: GraphDatabaseService, val storeDir: Path, val permanent: Boolean) extends TransactionSupport {

  def truncate() {
    transaction(db) { GlobalGraphOperations.at(db).getAllNodes.asScala.view.filter(isDeletable(_)).foreach(deleteNode(_)) }
  }

  private def isDeletable(n: Node) = n != db.getReferenceNode

  private def deleteNode(n: Node) {
    n.getRelationships.asScala.foreach(_.delete())
    n.delete()
  }

  def shutDown() {
    db.shutdown()
    if (!permanent) deleteStoreDir()
  }

  private def deleteStoreDir() {
    Files.walkFileTree(storeDir,
      new SimpleFileVisitor[Path] {

        override def visitFile(file: Path, attrs: BasicFileAttributes) = { Files.delete(file); CONTINUE }

        override def postVisitDirectory(dir: Path, e: IOException) = if (e == null) { Files.delete(dir); CONTINUE } else throw e
      }
    )
  }
}

private object Database {

  def apply(): Database = apply(UUID.randomUUID.toString)

  def apply(tempStoreDirPrefix: String, permanent: Boolean = false): Database = {
    val storeDir = Files.createTempDirectory(tempStoreDirPrefix)
    new Database(new TestGraphDatabaseFactory().newEmbeddedDatabase(storeDir.toString), storeDir, permanent)
  }
}
