package authentication

import argonaut.Argonaut._

case class AuthResponse(
                         access_token: String,
                         token_type: String,
                         expires_in: Int,
                         refresh_token: String
                       )

object AuthResponse {
  implicit def authResponseCodec =
    casecodec4(AuthResponse.apply, AuthResponse.unapply)("access_token", "token_type", "expires_in", "id_token")
}
