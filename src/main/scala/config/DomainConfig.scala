package config

import com.typesafe.config.{Config, ConfigFactory}

class DomainConfig(config: Config) {
  val settings = new DomainSettings(config)

  def this() {
    this(ConfigFactory.load())
  }
}

object DomainConfig extends DomainConfig() {}
