package config

import com.typesafe.config.{Config, ConfigFactory}

class DomainSettings(config: Config) {
  config.checkValid(ConfigFactory.defaultReference(), "domain")
  val domainConfig = config.getConfig("domain")
  val googleAuthConfig = domainConfig.getConfig("google_auth")
}
