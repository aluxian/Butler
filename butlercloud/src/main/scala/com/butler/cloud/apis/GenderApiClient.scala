package com.butler.cloud.apis

import java.io.InputStream

import akka.actor.{Actor, ActorLogging}
import akka.camel.{CamelMessage, Producer}
import com.butler.cloud.apis.GenderApiClient.{Get, Response}
import com.butler.cloud.utils.Settings
import net.liftweb.json._
import org.apache.camel.Exchange

import scala.io.Source

class GenderApiClient extends Actor with Producer with ActorLogging {

  implicit val formats = DefaultFormats
  val settings = Settings(context.system)
  def endpointUri = "ahc:" + settings.urls.genderApi

  override def transformOutgoingMessage(msg: Any): Any = msg match {
    case Get(name, country, language, email, ip) =>
      val query = Map(
        "name" -> name,
        "country" -> country,
        "language" -> language,
        "email" -> email,
        "ip" -> ip
      ).filter(_._2 != null)

      CamelMessage(null, Map(
        "X-Mashape-Key" -> settings.keys.gender,
        Exchange.HTTP_QUERY -> query.map { case (k, v) => k + "=" + v.toString}.mkString("&")
      ))
  }

  override def transformResponse(msg: Any): Any = msg match {
    case CamelMessage(body, headers) =>
      parse(Source.fromInputStream(body.asInstanceOf[InputStream]).mkString).extract[Response]
    case _ => msg
  }

}

object GenderApiClient {

  /**
   * Send a request with the given parameters. Not all of them are required.
   */
  case class Get(name: String = null, country: String = null, language: String = null, email: String = null, ip: String = null)

  /**
   * Response received from the API.
   */
  case class Response(name: String, country: String, gender: String, samples: Int, accuracy: Int, duration: String)

}
