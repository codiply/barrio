package com.codiply.barrio.helpers

import java.nio.file.Files
import java.nio.file.Paths

import scopt.OptionParser

object ArgsConfig {
  val defaultDimensions = 1
  val defaultMaxPointsPerLeaf = 128
  val defaultTreesPerNode = 3
}

case class ArgsConfig(
    file: String = "",
    dimensions: Int = ArgsConfig.defaultDimensions,
    maxPointsPerLeaf: Int = ArgsConfig.defaultMaxPointsPerLeaf,
    treesPerNode: Int = ArgsConfig.defaultTreesPerNode)

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

    opt[Int]('t', "treesPerNode")
      .maxOccurs(1)
      .validate(n =>
          if (n > 0) {
            success
          } else {
            failure("Value <treesPerNode> must be >0")
          })
      .action( (v, conf) => conf.copy(treesPerNode = v) )
      .text("the number of trees per node")

    opt[Int]('l', "maxPointsPerLeaf")
      .maxOccurs(1)
      .validate(n =>
          if (n > 0) {
            success
          } else {
            failure("Value <maxPointsPerLeaf> must be >0")
          })
      .action( (v, conf) => conf.copy(maxPointsPerLeaf = v) )
      .text("the maximum number of points per leaf")

    help("help").text("prints this usage text")
  }

  def parse(args: Seq[String]): Option[ArgsConfig] = {
    parser.parse(args, ArgsConfig())
  }
}
