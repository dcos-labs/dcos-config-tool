package de.ftrossbach.dcos.config.reader.domain



case class ApplicationVersionWithoutConfig(repositoryName: String, name: String, packageVersion: String)

case class ApplicationVersion(repositoryName: String, name: String, packageVersion: String, config: ObjectProperty)

case class Application(repositoryName: String, name: String, versions: Map[String, ApplicationVersion])


case class Repository(name: String, applications: Map[String, Application])

case class Repositories(repositories: Map[String, Repository])


trait ConfigurationProperty

case class StringProperty(name: String, description: Option[String], default: Option[String], required: Boolean, variableType: String = "string") extends ConfigurationProperty{
   def getPropertyType(): String = "string"
}

case class NumberProperty(name: String, description: Option[String], default: Option[Double], required: Boolean, variableType: String = "number") extends ConfigurationProperty

case class IntegerProperty(name: String, description: Option[String], default: Option[Int], required: Boolean, variableType: String = "integer") extends ConfigurationProperty

case class BooleanProperty(name: String, description: Option[String], default: Option[Boolean], required: Boolean, variableType: String = "boolean") extends ConfigurationProperty

case class ObjectProperty(name: String, description: Option[String], children: List[ConfigurationProperty], required: Boolean, variableType: String = "object") extends ConfigurationProperty


case class ArrayProperty(name: String, description: Option[String], item: List[ConfigurationProperty], required: Boolean, variableType: String = "array") extends ConfigurationProperty





