package models

case class UserInfo(
                     id: String,
                     name: String,
                     given_name: String,
                     family_name: String,
                     profile: String,
                     picture: String,
                     email: String,
                     email_verified: Boolean,
                     gender: String
                   )
