package de.ftrossbach.dcos.config.reader

import akka.actor.{Actor, ActorRef, Props}
import akka.actor.Actor.Receive
import de.ftrossbach.dcos.config.reader.ConfigReader.Update
import de.ftrossbach.dcos.config.reader.RepositoryFacade._
import de.ftrossbach.dcos.config.reader.domain._


object RepositoryFacade {

  case class AddRepository(name: String, url: String)

  case class GetRepositories()

  case class GetRepository(repositoryName: String)

  case class GetApplication(repositoryName: String, applicationName: String)

  case class GetApplicationVersion(repositoryName: String, applicationName: String, repositoryVersion: String)

}

class RepositoryFacade extends Actor {

  var repositories: Repositories = Repositories(Map())

  override def receive: Receive = {

    case AddRepository(name, url) => {
      if (!repositories.repositories.contains(name)) {
        val configReader = context.actorOf(ConfigReader.props(name, url))
        configReader ! Update()
      }
    }

    case x: Repository => {
      repositories = Repositories(repositories.repositories + (x.name -> x))
    }

    case GetRepositories() => sender ! repositories

    case GetRepository(repoName) => sender ! repositories.repositories.get(repoName).getOrElse(Repository(repoName, Map()))

    case GetApplication(repoName, applicationName) => {
      sender ! repositories.repositories.get(repoName).flatMap(repo => {
        repo.applications.get(applicationName)
      }).getOrElse(Application(applicationName, Map()))
    }

    case GetApplicationVersion(repositoryName, applicationName, applicationVersion) => {
      sender ! repositories.repositories.get(repositoryName).flatMap(repo => {
        repo.applications.get(applicationName)
      }).flatMap(appl => {
        appl.versions.get(applicationVersion)
      }).getOrElse(ApplicationVersion("UNKNOWN", ObjectProperty("UNKNOWN", None, List(), false)))

    }
  }
}
