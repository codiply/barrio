package com.codiply.barrio.configuration

import scopt.OptionParser

case class ArgsConfig(
    file: String = "", 
    algo: SearchAlgorithmEnum.Value = SearchAlgorithmEnum.Forest)

object SearchAlgorithmEnum extends Enumeration {
  type SearchAlgorithmEnum = Value
  val Linear, Forest = Value
}

object ArgsParser {
  private val parser = new OptionParser[ArgsConfig]("barrio") {
    head("barrio")
    
    opt[String]('f', "file")
      .action { (v, conf) => conf.copy(file = v) }
      .text("the path to a file with data points")
    
    opt[String]('a', "algo")
      .action { (v, conf) => {
          val algo = v.toLowerCase() match {
            case "linear" => SearchAlgorithmEnum.Linear
            case "forest" => SearchAlgorithmEnum.Forest
            case _ => SearchAlgorithmEnum.Forest
          }
          conf.copy(algo = algo)
        }
      }
      .text("the algorithm to be used, linear or forest, default is forest.")
      
    help("help").text("prints this usage text")
  }
  
  def parse(args: Seq[String]): ArgsConfig = {    
    parser.parse(args, ArgsConfig()) match {
      case Some(config) => config
      case None => ArgsConfig()
    }
  }
}
