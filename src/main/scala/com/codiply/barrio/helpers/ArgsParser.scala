package com.codiply.barrio.helpers

import java.nio.file.Files
import java.nio.file.Paths

import scopt.OptionParser

import com.codiply.barrio.geometry.Metric

object ArgsConfig {
  val defaultDimensions = 1
  val defaultMaxPointsPerLeaf = 128
  val defaultTreesPerNode = 3
}

case class ArgsConfig(
    coordinateSeparator: String = ",",
    dimensions: Int = ArgsConfig.defaultDimensions,
    file: String = "",
    separator: String = ":::",
    maxPointsPerLeaf: Int = ArgsConfig.defaultMaxPointsPerLeaf,
    metric: String = Metric.euclidean.name,
    randomSeed: Option[Int] = None,
    treesPerNode: Int = ArgsConfig.defaultTreesPerNode,
    isUrl: Boolean = false)

object ArgsParser {
  private val parser = new OptionParser[ArgsConfig]("barrio") {
    override def showUsageOnError = true

    head("Barrio", VersionHelper.version)

    help("help")
    version("version")

    opt[String]('f', "file")
      .required()
      .maxOccurs(1)
      .action { (v, conf) => conf.copy(file = v) }
      .text("the path to the input file containing the data points")

    opt[String]('m', "metric")
      .maxOccurs(1)
      .validate(m =>
          if (Metric.allMetrics.contains(m.toLowerCase)) {
            success
          } else {
            val options = Metric.allMetrics.keys.mkString(", ")
            failure(s"Unkown metric ${m}. Use one of the following options: ${options}.")
          })
      .action( (m, conf) => conf.copy(metric = m) )
      .text("the metric for calculating distances")

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

    opt[Int]('s', "randomSeed")
      .maxOccurs(1)
      .action( (v, conf) => conf.copy(randomSeed = Some(v)) )
      .text("the seed for the random number generator")

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

    opt[String]("separator")
      .maxOccurs(1)
      .action( (v, conf) => conf.copy(separator = v) )
      .text("the separator used in the input data for separating the id, the coordinates and additional data")

    opt[String]("coordinateSeparator")
      .maxOccurs(1)
      .action( (v, conf) => conf.copy(coordinateSeparator = v) )
      .text("the separator used in the input data for separating the coordinates within the coordinates field")

    opt[Unit]("isUrl")
      .maxOccurs(1)
      .action( (_, conf) => conf.copy(isUrl = true) )
      .text("flag for loading data from the web")

    help("help").text("prints this usage text")

    checkConfig(conf => {
      conf match {
        case _ if (!conf.isUrl && Files.exists(Paths.get(conf.file))) =>
          failure("Value <file> refers to non-existent file")
        case _ if (conf.separator == conf.coordinateSeparator) =>
          failure("value <separator> cannot be the same as <coordinateSeparator>")
        case _ => success
      }
    })
  }

  def parse(args: Seq[String]): Option[ArgsConfig] = {
    parser.parse(args, ArgsConfig())
  }
}
