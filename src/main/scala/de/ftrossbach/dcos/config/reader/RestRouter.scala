package de.ftrossbach.dcos.config.reader


import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directives, RequestContext, Route, StandardRoute}
import de.ftrossbach.dcos.config.reader.domain._
import akka.http.scaladsl.server.Directives._
import Main.system
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import com.lambdaworks.jacks.JacksMapper

import scala.concurrent.duration._
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{SerializationFeature, SerializerProvider}
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import de.ftrossbach.dcos.config.reader.RepositoryFacade.{GetApplication, GetApplicationVersion, GetRepositories, GetRepository}

import scala.concurrent.{ExecutionContext, Future}


class RestRouter extends Directives {
  implicit val ec = ExecutionContext.Implicits.global

  def route(repoFacade: ActorRef) = {
    implicit val timeout = Timeout(5 seconds)


    pathPrefix("api" / "repository") {

      pathEndOrSingleSlash {
        get {
          marshal {
            (repoFacade ? GetRepositories()).mapTo[Repositories]
          }
        }
      } ~
        pathPrefix(Segment) {
          repo =>
            pathEndOrSingleSlash {
              get {
                marshal {
                  (repoFacade ? GetRepository(repo)).mapTo[Repository]
                }
              }
            } ~
              pathPrefix(Segment) {
                application =>
                  pathEndOrSingleSlash {
                    get {
                      marshal {
                        (repoFacade ? GetApplication(repo, application)).mapTo[Application]
                      }
                    }
                  } ~ pathPrefix(Segment) {
                    version =>
                      pathEndOrSingleSlash {
                        get {
                          marshal {
                            (repoFacade ? GetApplicationVersion(repo, application, version)).mapTo[ApplicationVersion]
                          }
                        }
                      }
                  }

              }

        }
    }
  }

  def marshal(m: => Future[Any]): StandardRoute =
    StandardRoute(ctx => {
      ctx.complete({
        JacksMapper.mapper.enable(SerializationFeature.INDENT_OUTPUT)
        m.map(JacksMapper.writeValueAsString(_))
      })


    })



}

