package authentication

import argonaut._
import Argonaut._

object DefaultDiscoveryDoc {
  case class discoveryStructure(auth_ep: String, token_ep: String, ui_ep: String)

  implicit def discoveryCodec =
    casecodec3(discoveryStructure.apply, discoveryStructure.unapply)("authorization_endpoint", "token_endpoint", "userinfo_endpoint")

  private val js =
    """
      |{
      | "issuer": "https://accounts.google.com",
      | "authorization_endpoint": "https://accounts.google.com/o/oauth2/v2/auth",
      | "token_endpoint": "https://www.googleapis.com/oauth2/v4/token",
      | "userinfo_endpoint": "https://www.googleapis.com/oauth2/v3/userinfo",
      | "revocation_endpoint": "https://accounts.google.com/o/oauth2/revoke",
      | "jwks_uri": "https://www.googleapis.com/oauth2/v3/certs",
      | "response_types_supported": [
      |  "code",
      |  "token",
      |  "id_token",
      |  "code token",
      |  "code id_token",
      |  "token id_token",
      |  "code token id_token",
      |  "none"
      | ],
      | "subject_types_supported": [
      |  "public"
      | ],
      | "id_token_signing_alg_values_supported": [
      |  "RS256"
      | ],
      | "scopes_supported": [
      |  "openid",
      |  "email",
      |  "profile"
      | ],
      | "token_endpoint_auth_methods_supported": [
      |  "client_secret_post",
      |  "client_secret_basic"
      | ],
      | "claims_supported": [
      |  "aud",
      |  "email",
      |  "email_verified",
      |  "exp",
      |  "family_name",
      |  "given_name",
      |  "iat",
      |  "iss",
      |  "locale",
      |  "name",
      |  "picture",
      |  "sub"
      | ],
      | "code_challenge_methods_supported": [
      |  "plain",
      |  "S256"
      | ]
      |}
    """.stripMargin

  val doc = js.decode[discoveryStructure].toList.head
}