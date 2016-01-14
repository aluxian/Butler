package com.butler.cloud.apis

import java.io.InputStream

import akka.actor.{Actor, ActorLogging}
import akka.camel.{CamelMessage, Producer}
import com.butler.cloud.apis.FreebaseApiClient._
import com.butler.cloud.utils.Settings
import net.liftweb.json._
import org.apache.camel.Exchange

import scala.io.Source

class FreebaseApiClient extends Actor with Producer with ActorLogging {

  implicit val formats = DefaultFormats
  val settings = Settings(context.system)
  def endpointUri = "ahc:" + settings.urls.freebaseApi + ""

  override def transformOutgoingMessage(msg: Any): Any = msg match {
    case Search(searchQuery, filter, limit, spell) =>
      val query = Map(
        "key" -> settings.keys.google,
        "query" -> searchQuery,
        "filter" -> filter,
        "limit" -> limit,
        "spell" -> spell
      ).filter(_._2 != null)

      CamelMessage(null, Map(
        Exchange.HTTP_URI -> s"${settings.urls.freebaseApi}/search",
        Exchange.HTTP_QUERY -> query.map { case (k, v) => k + "=" + v.toString}.mkString("&")
      ))

    case Lookup(id, dateline, filter, lang, limit, raw) =>
      val query = Map(
        "key" -> settings.keys.google,
        "dateline" -> dateline,
        "filter" -> filter,
        "lang" -> lang,
        "limit" -> limit,
        "raw" -> raw
      ).filter(_._2 != null)

      CamelMessage(null, Map(
        Exchange.HTTP_URI -> s"${settings.urls.freebaseApi}/topic$id",
        Exchange.HTTP_QUERY -> query.map { case (k, v) => k + "=" + v.toString}.mkString("&")
      ))
  }

  override def transformResponse(msg: Any): Any = msg match {
    case CamelMessage(body, headers) =>
      if (headers.get("CamelHttpUri").get.asInstanceOf[String] == s"${settings.urls.freebaseApi}/search") {
        parse(Source.fromInputStream(body.asInstanceOf[InputStream]).mkString).extract[SearchResponse]
      } else {
        parse(Source.fromInputStream(body.asInstanceOf[InputStream]).mkString).extract[LookupResult]
      }
    case _ => msg
  }

}

object FreebaseApiClient {

  /**
   * Perform a search request.
   * Reference: https://developers.google.com/freebase/v1/search.
   *
   * @param query Query term to search for.
   * @param filter The filter parameter allows you to create more complex rules and constraints to apply to the query.
   * @param limit Maximum number of results to return. By default, 20 matches in decreasing order of relevance are returned.
   * @param spell Request 'did you mean' suggestions.
   */
  case class Search(query: String = null, filter: String = null, limit: Int = 20, spell: String = "always")
  // TODO: Add more parameters.

  /**
   * Perform a lookup.
   * Reference: https://developers.google.com/freebase/v1/topic.
   *
   * @param id The id of the resource.
   * @param dateline Determines how up-to-date the data returned is. A UNIX epoch time, a guid or now.
   * @param filter Return only appropriate properties. A Freebase domain, type or property ID. suggest, commons, or all.
   * @param lang The language you'd like the content in – a Freebase /type/lang language key.
   * @param limit	The maximum number of property values to return for each property. Must be a positive integer.
   * @param raw Do not apply any constraints, or get any names.
   */
  case class Lookup(id: String, dateline: String = null, filter: String = null, lang: String = null, limit: Int = 10, raw: Boolean = false)

  /**
   * Object returned by the Freebase Topic API.
   *
   * @param id The id of the resource.
   * @param property Properties object.
   */
  case class LookupResult(id: String, property: LookupProperty)

  /**
   * List of properties returned from a lookup.
   *
   * @param `/common/topic/description` Topic description.
   * @param `/common/topic/image` Topic image.
   */
  case class LookupProperty(`/common/topic/description`: LookupValues,
                            `/common/topic/image`: LookupValues)

  /**
   * Wrapper for a property value.
   *
   * @param valuetype The type of the values.
   * @param values List of values.
   */
  case class LookupValues(valuetype: String, values: List[LookupValue])

  /**
   * Stores a property's value.
   *
   * @param text Value text.
   * @param id Value ID.
   * @param lang Value language.
   * @param value The value itself.
   * @param citation Value citation.
   */
  case class LookupValue(text: String, id: Option[String], lang: String, value: Option[String], citation: Option[LookupCitation])

  /**
   * Stores a citation.
   *
   * @param provider Citation provider.
   * @param statement Citation statement.
   * @param uri Citation URI.
   */
  case class LookupCitation(provider: String, statement: String, uri: String)

  /**
   * Object returned by the Freebase Search API.
   *
   * @param status Request status.
   * @param result List of results.
   * @param correction Suggested correction of the search.
   * @param hits Number of hits.
   */
  case class SearchResponse(status: String, result: List[SearchResult], correction: List[String], hits: Int)

  /**
   * Stores a single search result.
   *
   * @param mid Result MID.
   * @param name Result name.
   * @param notable Notable object.
   * @param score Result score.
   */
  case class SearchResult(mid: String, name: String, notable: Option[SearchNotable], score: Float)

  /**
   * Stores the notable of a search result.
   *
   * @param name Notable name.
   * @param id Notable ID.
   */
  case class SearchNotable(name: String, id: String)

}
