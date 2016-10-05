package config

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server.Directives._
import authentication.Authenticator
import de.heikoseeberger.akkahttpargonaut.ArgonautSupport

import scala.concurrent.Future

class Routes extends ArgonautSupport with AuthenticatedRoutes {

  import main.Application._

  private val root = redirect("/", StatusCodes.TemporaryRedirect)
  private val dashboard = redirect("/", StatusCodes.TemporaryRedirect)

  val log = system.log

  private def handleException[U]: PartialFunction[Throwable, Future[Option[U]]] = {
    case ex: Throwable => {
      ex.printStackTrace()
      log.error(ex.getMessage)
      Future.successful(None)
    }
  }

  private val handleRoot = (path("") & get) {
    complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "Root"))
  }

  private val handleUnauthenticatedRequest = extractUri { uri =>
    val f =
      Authenticator
        .getAuthUrl(uri.toString)
        .recoverWith(handleException)
    onSuccess(f) { ruOpt =>
      ruOpt map { ru =>
        redirect(ru, StatusCodes.TemporaryRedirect)
      } getOrElse {
        complete(StatusCodes.InternalServerError)
      }
    }
  }

  private val handleOAuth2Callback = (path("oauth2callback") & get) {
    (parameter('code) & parameter('state)) { case (code, state) =>
      val f =
        Authenticator
          .setToken(code, state)
          .recoverWith(handleException)
      onSuccess(f) {
        case Some((session, url)) => {
          setCookie(HttpCookie("SessionId", session)) {
            redirect(url, StatusCodes.TemporaryRedirect)
          }
        }
        case _ => complete(StatusCodes.Forbidden)
      } ~ complete(StatusCodes.InternalServerError)
    } ~ root
  }

  private val handleAuthenticatedRequest = cookie("SessionId") { session =>
    val f =
      Authenticator
        .validate(session.value)
        .run
        .recoverWith(handleException)
    onSuccess(f) {
      case Some(ui) => authenticatedRoutes(ui)
      case None => {
        deleteCookie("SessionId") {
          handleUnauthenticatedRequest
        }
      }
    } ~ complete(StatusCodes.InternalServerError)
  }

  val route =
    handleRoot ~
      handleAuthenticatedRequest ~
      handleOAuth2Callback ~
      handleUnauthenticatedRequest
}
