package config

import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpargonaut.ArgonautSupport
import models.UserInfo

trait AuthenticatedRoutes extends ArgonautSupport {

  protected def authenticatedRoutes(ui: UserInfo) = {
    // Routes for properly authenticated requests go here
    complete(ui.toString)
  }
}
