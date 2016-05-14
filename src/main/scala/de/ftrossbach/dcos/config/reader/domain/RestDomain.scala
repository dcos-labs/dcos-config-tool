package de.ftrossbach.dcos.config.reader.domain





case class ApplicationVersion(packageVersion: String, config: ObjectProperty)

case class Application(name: String, versions: Map[String, ApplicationVersion])


case class Repository(name: String, applications: Map[String, Application])

case class Repositories(repositories: Map[String, Repository])


trait ConfigurationProperty

case class StringProperty(name: String, description: Option[String], default: Option[String], required: Boolean) extends ConfigurationProperty{
   def getPropertyType(): String = "string"
}

case class NumberProperty(name: String, description: Option[String], default: Option[Double], required: Boolean) extends ConfigurationProperty

case class IntegerProperty(name: String, description: Option[String], default: Option[Int], required: Boolean) extends ConfigurationProperty

case class BooleanProperty(name: String, description: Option[String], default: Option[Boolean], required: Boolean) extends ConfigurationProperty

case class ObjectProperty(name: String, description: Option[String], children: List[ConfigurationProperty], required: Boolean) extends ConfigurationProperty


case class ArrayProperty(name: String, description: Option[String], item: List[ConfigurationProperty], required: Boolean) extends ConfigurationProperty





