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

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}


class RestRouter extends Directives {
  implicit val ec = ExecutionContext.Implicits.global

  def route(repoFacade: ActorRef) = {
    implicit val timeout = Timeout(5 seconds)



    pathPrefix("") {

      pathEndOrSingleSlash {
        redirect("/index.html", StatusCodes.PermanentRedirect)
      } ~
        encodeResponse {
          get {
            getFromResourceDirectory("assets")
          }
        }
    } ~
      pathPrefix("api" / "repository") {

        encodeResponse {
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
      } ~
      path("api" / "generate") {
        get {
          parameterSeq { params => {
            marshal {
              Future({
                val acc = mutable.HashMap.empty[String, Any]
                paramsToMap(params.toList, acc)
                acc
              })

            }
          }
          }
        } }~
          path("api" / "list") {
            get {

              marshal {
                (repoFacade ? GetRepositories()).mapTo[Repositories].map(repositories => {

                  val versions: Iterable[ApplicationVersion] = for (repo <- repositories.repositories;
                                                                    app <- repo._2.applications;
                                                                    appVer <- app._2.versions
                  ) yield appVer._2

                  versions.map(ver => ApplicationVersionWithoutConfig(ver.repositoryName, ver.name, ver.packageVersion))


                })
              }
            }

      }

  }


  def paramsToMap(params: List[(String, String)], acc: mutable.HashMap[String, Any]): Unit = {

    params match {
      case Nil => acc

      case head :: tail => {

        val strippedHead = if (head._1.startsWith("/")) head._1.substring(1) else head._1

        val segments: Array[String] = strippedHead.split("/")

        val reverse = segments.reverse
        val valueType = reverse.head

        val variableName = reverse.tail.head

        val path = reverse.tail.tail.reverse.toList


        applyParamToMap(variableName, head._2, valueType, path, acc)

        paramsToMap(tail, acc)


      }

    }


  }


  def applyParamToMap(name: String, value: String, valType: String, path: List[String], current: mutable.HashMap[String, Any]): Unit = {

    path match {
      case Nil => {
        valType match {
          case "s" => current += (name -> value)
          case "d" => current += (name -> value.toDouble)
          case "i" => current += (name -> value.toInt)
          case "n" => current += (name -> value.toInt)
          case "b" => current += (name -> value.toBoolean)
        }
      }

      case head :: tail => {

        if (current.contains(head)) {
          applyParamToMap(name, value, valType, tail, current.get(head).get.asInstanceOf[mutable.HashMap[String, Any]])
        } else {
          val newMap = mutable.HashMap.empty[String, Any]
          current += (head -> newMap)
          applyParamToMap(name, value, valType, tail, newMap)
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

