package com.github.sandrasi.moviecatalog.repository

import com.github.sandrasi.moviecatalog.domain.Entity
import java.io.Closeable
import java.util.{Locale, UUID}
import scala.collection.mutable.{Map => MutableMap}

trait Repository {

  def get[A <: Entity](id: UUID, entityType: Class[A])(implicit locale: Locale): Option[A]

  def save[A <: Entity](entity: A)(implicit locale: Locale): A
  
  def delete(entity: Entity)

  def query[A <: Entity](entityType: Class[A], predicate: A => Boolean = (_: A) => true): Iterator[A]

  def search(text: String)(implicit locale: Locale): Iterator[Entity]

  def shutdown()
}

trait RepositoryFactory {

  def apply(repositoryConfiguration: RepositoryConfiguration): Repository

  def configurationMetaData: ConfigurationMetaData

  type ParameterConversionResult[A] = Either[Exception, A]

  type ParameterConverter[A] = Seq[String] => ParameterConversionResult[A]

  case class ConfigurationParameterMetaData[A](name: String, description: String, valueType: Class[A], parameterConverter: ParameterConverter[A])

  case class ConfigurationMetaData(configurationParameters: ConfigurationParameterMetaData[_]*) {

    private val cfgParams: Map[String, ConfigurationParameterMetaData[_]] = configurationParameters.map(cp => cp.name -> cp).toMap

    def get(parameterName: String): Option[ConfigurationParameterMetaData[_]] = cfgParams.get(parameterName)
  }

  class RepositoryConfiguration {

    private val parameters = MutableMap[String, Any]()

    def get[A](name: String, valueType: Class[A]): A = parameters(name).asInstanceOf[A]

    def set(name: String, value: Any): RepositoryConfiguration = {
      parameters.put(name, value)
      this
    }

    def setFromString[A](name: String, values: String*)(implicit parameterConverter: ParameterConverter[A]): RepositoryConfiguration = {
      val convertedValue = parameterConverter(values).fold(
        error => throw new IllegalArgumentException("Conversion exception", error),
        value => value
      )
      set(name, convertedValue)
    }
  }
}
