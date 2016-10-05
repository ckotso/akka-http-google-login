package authentication

import java.math.BigInteger
import java.security.SecureRandom

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import argonaut.Argonaut._
import argonaut._
import implicits.userInfoCodec
import models.UserInfo

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scalaz.OptionT
import OptionT._
import scalaz.std.scalaFuture._

object Authenticator {

  import main.Application._

  val domainConfig = config.DomainConfig.settings

  private val clientId = domainConfig.googleAuthConfig.getString("clientId")
  private val clientSecret = domainConfig.googleAuthConfig.getString("clientSecret")
  private val callbackUrl = domainConfig.googleAuthConfig.getString("callback")
  private val scope = domainConfig.googleAuthConfig.getString("scope")

  private val discoveryDocUrl = domainConfig.googleAuthConfig.getString("discoveryDocUrl")
  private val discoveryDoc = onBoot.getOrElse(DefaultDiscoveryDoc.doc)

  private val authorizationEndpoint = discoveryDoc.map(_.auth_ep)
  private val tokenEndpoint = discoveryDoc.map(_.token_ep)
  private val userInfoEndpoint = discoveryDoc.map(_.ui_ep)

  private val userMap = new TrieMap[String, UserInfo]()
  private val sessionMap = new TrieMap[String, String]()

  private def onBoot = {
    import DefaultDiscoveryDoc.{discoveryCodec, discoveryStructure}

    val request = HttpRequest(uri = Uri(discoveryDocUrl))
    val fo = for {
      response <- Http().singleRequest(request)
      body <- Unmarshal(response.entity).to[String]
    } yield body.decodeOption[discoveryStructure]

    optionT(fo)
  }

  def getAuthUrl(uri: String) = {
    val state = new BigInteger(130, new SecureRandom()).toString(32)
    sessionMap += ((state, uri))
    authorizationEndpoint map { ae =>
      Some("%s?client_id=%s&redirect_uri=%s&response_type=code&scope=%s&access_type=online&state=%s".format(
        ae, clientId, callbackUrl, scope, state
      )).asInstanceOf[Option[String]]
    }
  }

  private def getAccessToken(code: String) = {
    val requestObject = Map[String, String](
      "code" -> code,
      "client_id" -> clientId,
      "client_secret" -> clientSecret,
      "redirect_uri" -> callbackUrl,
      "grant_type" -> "authorization_code"
    )

    val entity = FormData(requestObject).toEntity

    for {
      r <- tokenEndpoint.map(te => HttpRequest(method = HttpMethods.POST, uri = Uri(te), entity = entity))
      h <- Http().singleRequest(r)
      s <- Unmarshal(h.entity).to[String]
    } yield s.decodeOption[AuthResponse].getOrElse(throw new Exception("Bad authentication response"))
  }

  private def getUserInfo(access_token: String) = {
    val authHeader = HttpHeader.parse("Authorization", s"Bearer $access_token") match {
      case HttpHeader.ParsingResult.Ok(h, Nil) => h
      case _ => throw new Exception("Invalid header")
    }

    for {
      req <- userInfoEndpoint.map(ue => HttpRequest(uri = Uri(ue)).withHeaders(authHeader))
      rsp <- Http().singleRequest(req)
      body <- Unmarshal(rsp.entity).to[String]
    } yield body.decodeOption[UserInfo].getOrElse(throw new Exception("Bad user info"))
  }

  def setToken(code: String, state: String) = {
    sessionMap.get(state) map { rurl =>
      (for {
        ac <- getAccessToken(code).map(_.access_token)
        ui <- getUserInfo(ac)
      } yield ui) map { ui =>
        val newState = new BigInteger(130, new SecureRandom()).toString(32)
        sessionMap -= state
        userMap += ((newState, ui))
        Some((newState, rurl))
      }
    } getOrElse {
      val r: Option[(String, String)] = None
      Future.successful(r)
    }
  }

  def validate(session: String) = optionT(Future.successful(userMap.get(session)))
}
