import argonaut.Argonaut._
import models.UserInfo

package object implicits {
  implicit def userInfoCodec =
    casecodec9(UserInfo.apply, UserInfo.unapply)(
      "sub", "name", "given_name", "family_name", "profile", "picture", "email", "email_verified", "gender"
    )
}
