package de.ftrossbach.dcos.config.reader

import java.io.ByteArrayInputStream
import java.net.URL
import java.util.Optional
import java.util.zip.ZipInputStream

import akka.actor.{Actor, ActorLogging, Props}
import akka.actor.Actor.Receive
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import de.ftrossbach.dcos.config.reader.ConfigReader._
import de.ftrossbach.dcos.config.reader.RepositoryFacade.GetRepository
import de.ftrossbach.dcos.config.reader.domain._
import org.apache.commons.io.IOUtils

import scala.concurrent.Future


object ConfigReader {
  case class Update()

  case class AddConfiguration(name: String, version: String, properties: ObjectProperty)

  case class AddPackage(name: String, version: String, packageVersion: String)


  def props(name: String, url: String): Props = Props(new ConfigReader(name, url))


}

class ConfigReader(name: String, url: String) extends Actor with ActorLogging{

  implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))


  var stateMap: Map[(String, String), (Option[String], Option[ObjectProperty]) ] = Map()


  override def receive: Receive = {
    case Update() => triggerStateUpdate(url)
    case response@HttpResponse(StatusCodes.OK, headers, entity, _) => updateStatus(response)

    case response@HttpResponse(StatusCodes.Found, headers, entity, _) => redirect(response)
    case HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)

    case AddConfiguration(name, version, config) => {
      val tuple = stateMap.get((name, version)).getOrElse((None, None))
      stateMap = stateMap + ((name, version) -> (tuple._1, Some(config)))
      updateRepository()

    }
    case AddPackage(name, version, packageVersion) => {
      val tuple = stateMap.get((name, version)).getOrElse((None, None))
      stateMap = stateMap + ((name, version) -> (Some(packageVersion), tuple._2))
      updateRepository()

    }


  }

  def updateRepository(): Unit ={
    val completeVersions = stateMap.filter(entry => entry._2._1.isDefined && entry._2._2.isDefined)

    val list: List[(String, String, ObjectProperty)] = completeVersions.map(entry => (entry._1._1, entry._2._1.get, entry._2._2.get)).toList

    val app: List[Application] = list.groupBy(triple => triple._1).map(groupedTriple => Application(groupedTriple._1, groupedTriple._2.map(x => x._2 -> ApplicationVersion(x._2, x._3)).toMap)).toList

    val appMap: Map[String, Application] = app.map(app => app.name -> app).toMap
    context.parent !  Repository(name, appMap)

  }


  def redirect(response: HttpResponse) = {

    response.headers.find(_.is("location")).foreach(header => triggerStateUpdate(header.value()))


  }

  def updateStatus(response: HttpResponse): Unit = {
    import context._


   val future: Future[Array[Byte]] = Unmarshal(response).to[Array[Byte]]

    future.foreach(byteArray => {
      val stream = new ZipInputStream(new ByteArrayInputStream(byteArray))

      val regex = ".*/repo/packages/[A-Z]/(.*)/(\\d*)/(config.json|package.json)".r("name", "version", "type")

      var zipEntry = stream.getNextEntry



      while(zipEntry != null){

        regex.findFirstMatchIn(zipEntry.getName).foreach(regMatch => {
          val name = regMatch.group("name")
          val version = regMatch.group("version")

          val fileType = regMatch.group("type")

          val content = IOUtils.toString(stream, "UTF-8")

          val msg = Parse(name, version, content)

          fileType match {
            case "config.json" => context.actorOf(Props[ConfigParser]) ! msg
            case "package.json" => context.actorOf(Props[PackageParser]) ! msg
          }
        })



        zipEntry = stream.getNextEntry
      }
    })

  }


  def triggerStateUpdate(url: String): Unit = {
    import akka.pattern.pipe
    import context.dispatcher



    val http = Http(context.system)

    http.singleRequest(HttpRequest(uri = url))
      .pipeTo(self)

  }

}
