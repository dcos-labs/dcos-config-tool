package de.ftrossbach.dcos.config.reader

import akka.actor.Actor

import com.lambdaworks.jacks.JacksMapper

import de.ftrossbach.dcos.config.reader.ConfigReader.AddConfiguration


object ConfigParser {

}

class ConfigParser extends Actor{


  override def receive: Receive = {
    case Parse(name, version, json) => {
       val map = JacksMapper.readValue[Map[String, Any]](json)
       context.parent ! AddConfiguration(name, version, map)

    }
  }
}
