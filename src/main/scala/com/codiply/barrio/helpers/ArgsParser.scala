package com.codiply.barrio.helpers

import java.nio.file.Files
import java.nio.file.Paths

import scopt.OptionParser

case class ArgsConfig(
    file: String = "",
    dimensions: Int = 1)

object ArgsParser {
  private val parser = new OptionParser[ArgsConfig]("barrio") {
    override def showUsageOnError = true

    head("Barrio", VersionHelper.version)

    help("help")
    version("version")

    opt[String]('f', "file")
      .required()
      .maxOccurs(1)
      .validate(f =>
          if (Files.exists(Paths.get(f))) {
            success
          } else {
            failure("Value <file> refers to non-existent file")
          })
      .action { (v, conf) => conf.copy(file = v) }
      .text("the path to the input file containing the data points")

    opt[Int]('d', "dimensions")
      .required()
      .maxOccurs(1)
      .validate(d =>
          if (d > 0) {
            success
          } else {
            failure("Value <dimensions> must be >0")
          })
      .action( (v, conf) => conf.copy(dimensions = v) )
      .text("the number of dimensions")

    help("help").text("prints this usage text")
  }

  def parse(args: Seq[String]): Option[ArgsConfig] = {
    parser.parse(args, ArgsConfig())
  }
}

