package de.ftrossbach.dcos.config.reader

import java.net.URL

import akka.actor.{ActorSystem, Props}
import de.ftrossbach.dcos.config.reader.ConfigReader.{GetRepository, Update}

import scala.concurrent.duration._
/**
  * Created by ftr on 13/05/16.
  */
object Main {

  def main(args: Array[String]) {


    val system =  ActorSystem("dcos-config")
    import system.dispatcher

    val configReader = system.actorOf(Props[ConfigReader])


    configReader ! Update(new URL("https://universe.mesosphere.com/repo"))

    system.scheduler.schedule(5 seconds, 5 seconds, configReader, GetRepository())
  }

}
