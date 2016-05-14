package de.ftrossbach.dcos.config.reader

import akka.actor.{Actor, ActorLogging}
import com.fasterxml.jackson.databind.JsonNode
import com.lambdaworks.jacks.JacksMapper
import de.ftrossbach.dcos.config.reader.ConfigReader.AddConfiguration
import de.ftrossbach.dcos.config.reader.domain.{ArrayProperty, BooleanProperty, ConfigurationProperty, IntegerProperty, NumberProperty, ObjectProperty, StringProperty}
import de.ftrossbach.dcos.config.reader.domain._

import collection.JavaConverters._

object ConfigParser {

}

class ConfigParser extends Actor with ActorLogging {


  override def receive: Receive = {
    case Parse(name, version, json) => {
      val map = JacksMapper.readValue[Map[String, Any]](json)


      val jsonNode = JacksMapper.readValue[JsonNode](json)

      val objectProperty = construct("", jsonNode, List()).map(_.asInstanceOf[ObjectProperty]).foreach(context.parent ! AddConfiguration(name, version, _))


    }
  }

  def construct(name: String, node: JsonNode, required: List[String]): Option[ConfigurationProperty] = {

    log.info(s"Constructing node $name")

    val nodeTypeOption = Option(node.get("type")).map(_.asText())


    nodeTypeOption.map(nodeType => {

      nodeType match {

        case "string" => StringProperty(name, fieldAsOptionalText(node, "description"), fieldAsOptionalText(node, "default"), isRequired(name, node, required))
        case "number" => NumberProperty(name, fieldAsOptionalText(node, "description"), fieldAsOptionalNumber(node, "default"), isRequired(name, node, required))
        case "integer" => IntegerProperty(name, fieldAsOptionalText(node, "description"), fieldAsOptionalInteger(node, "default"), isRequired(name, node, required))
        case "boolean" => BooleanProperty(name, fieldAsOptionalText(node, "description"), fieldAsOptionalBoolean(node, "default"), isRequired(name, node, required))

        case "object" => {
          val properties = node.get("properties")
          val required = List()
          val objectProperties: List[ConfigurationProperty] = properties.fields().asScala.map(tuple => construct(s"$name/${tuple.getKey}", tuple.getValue, required)).toList.filter(_.isDefined).map(_.get)
          ObjectProperty(name, fieldAsOptionalText(node, "description"), objectProperties, isRequired(name, node, required))
        }

        case "array" => {
          val items = node.get("items")
          val required = List()

          val itemProperty = construct(s"$name/[0]", items, required).get
          ArrayProperty(name, fieldAsOptionalText(node, "description"), List(itemProperty), isRequired(name, node, required))
        }
      }
    })

  }

  def fieldAsText(node: JsonNode, field: String): String = {
    node.get(field).asText()
  }

  def fieldAsOptionalText(node: JsonNode, field: String): Option[String] = {
    Option(node.get(field)).filter(node => node.isTextual).map(node => node.asText())
  }

  def fieldAsOptionalNumber(node: JsonNode, field: String): Option[Double] = {
    Option(node.get(field)).filter(node => node.isNumber).map(node => node.asDouble())
  }

  def fieldAsOptionalInteger(node: JsonNode, field: String): Option[Int] = {
    Option(node.get(field)).filter(node => node.isInt).map(node => node.asInt())
  }

  def fieldAsOptionalBoolean(node: JsonNode, field: String): Option[Boolean] = {
    Option(node.get(field)).filter(node => node.isBoolean).map(node => node.asBoolean())
  }


  def isRequired(name: String, node: JsonNode, required: List[String]) = {
    required.contains(name)
  }

}
