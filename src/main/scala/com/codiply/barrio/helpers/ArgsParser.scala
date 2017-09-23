package com.codiply.barrio.helpers

import java.nio.file.Files
import java.nio.file.Paths

import scopt.OptionParser

import com.codiply.barrio.geometry.Metric

object ArgsConfig {
  val defaultMaxPointsPerLeaf = 128
  val defaultTreesPerNode = 3
}

sealed trait DataSourceType

object DataSourceType {
  case object LocalDataSource extends DataSourceType
  case object WebDataSource extends DataSourceType
  case object S3DataSource extends DataSourceType
}

case class ArgsConfig(
    cache: Boolean = false,
    coordinateSeparator: String = ",",
    dimensions: Int = -1,
    encoding: String = "UTF-8",
    file: String = "",
    separator: String = ":::",
    maxPointsPerLeaf: Int = ArgsConfig.defaultMaxPointsPerLeaf,
    metric: String = Metric.euclidean.name,
    randomSeed: Option[Int] = None,
    s3Bucket: Option[String] = None,
    seedOnlyNode: Boolean = false,
    treesPerNode: Int = ArgsConfig.defaultTreesPerNode,
    isUrl: Boolean = false) {
  import DataSourceType._

  val dataSourceType =
    (isUrl, s3Bucket) match {
      case (true, _) => WebDataSource
      case (_, Some(_)) => S3DataSource
      case _ => LocalDataSource
    }
}

object ArgsParser {
  import DataSourceType._

  private val parser = new OptionParser[ArgsConfig]("barrio") {
    override def showUsageOnError = true

    head("Barrio", VersionHelper.version)

    help("help")
    version("version")

    opt[Unit]("seedOnlyNode")
      .maxOccurs(1)
      .action { (v, conf) => conf.copy(seedOnlyNode = true) }
      .text("flag for making this node act as a seed for the cluster only")

    opt[String]('f', "file")
      .maxOccurs(1)
      .action { (v, conf) => conf.copy(file = v) }
      .text("the path to the input file containing the data points")

    opt[String]("encoding")
      .maxOccurs(1)
      .action { (v, conf) => conf.copy(encoding = v)}
      .text("the encoding to be used when loading the data")

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

    opt[String]("s3Bucket")
      .maxOccurs(1)
      .action( (v, conf) => conf.copy(s3Bucket = Some(v)) )
      .text("S3 bucket containing the data file")

    opt[Unit]("cache")
      .maxOccurs(1)
      .action( (_, conf) => conf.copy(cache = true) )
      .text("flag for caching responses (when possible)")

    help("help").text("prints this usage text")

    checkConfig(conf => {
      conf match {
        case _ if (!conf.seedOnlyNode && conf.file.isEmpty()) =>
          failure("Missing option --file")
        case _ if (!conf.seedOnlyNode && conf.dimensions < 0) =>
          failure("Missing option --dimensions")
        case _ if (conf.dataSourceType == LocalDataSource && !Files.exists(Paths.get(conf.file))) =>
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
