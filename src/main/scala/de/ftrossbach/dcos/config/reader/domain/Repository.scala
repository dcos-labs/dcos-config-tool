package de.ftrossbach.dcos.config.reader.domain



case class ApplicationVersion(packageVersion: String, config: Map[String, Any])
case class Application(name: String, versions: List[ApplicationVersion])
case class Repository(name: String, applications: List[Application])


