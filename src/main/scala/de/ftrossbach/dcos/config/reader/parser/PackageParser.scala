package de.ftrossbach.dcos.config.reader.parser

import akka.actor.Actor
import com.lambdaworks.jacks.JacksMapper
import de.ftrossbach.dcos.config.reader.ConfigReader.AddPackage




object PackageParser {

}

class PackageParser extends Actor{


  override def receive: Receive = {
    case Parse(name, version, json) => {
      val map = JacksMapper.readValue[Map[String, Any]](json)
      context.parent ! AddPackage(name, version, map.get("version").getOrElse("unknown").asInstanceOf[String])
      context.stop(self)
    }
  }
}