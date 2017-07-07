package com.codiply.barrio

import scopt.OptionParser

case class ArgsConfig(file: String = "")

object ArgsParser {
  private val parser = new OptionParser[ArgsConfig]("barrio") {
    head("barrio")
    
    opt[String]('f', "file")
      .action { (v, conf) => conf.copy(file = v) }
      .text("the path to a file with points")
    
    help("help").text("prints this usage text")
  }
  
  def parse(args: Seq[String]) = {    
    parser.parse(args, ArgsConfig()) match {
      case Some(config) => config
      case None => ArgsConfig()
    }
  }
}
