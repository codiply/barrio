package com.codiply.barrio.neighbors

import scala.reflect.ClassTag

case class ClusterHealth(errors: List[String])

object ClusterHealthAnalyzer {
  def analyse(stats: ClusterStats): ClusterHealth = {
    val errorChecks = List(checkVersion(_), checkDimensions(_))
    val errors = errorChecks.flatMap(_(stats))
    ClusterHealth(errors)
  }

  private def checkVersion(stats: ClusterStats): List[String] =
    checkConsistencyBetweenNodes(stats, _.version, "version")

  private def checkDimensions(stats: ClusterStats): List[String] =
    checkConsistencyBetweenNodes(stats, _.dimensions, "dimensions")

  private def checkConsistencyBetweenNodes[T: ClassTag](
      stats: ClusterStats,
      fieldSelector: NodeStats => T,
      fieldName: String): List[String] = {
    if (stats.nodes.size < 1) {
      List[String]()
    } else {
      val nodeFieldValues = stats.nodes.mapValues(fieldSelector)
      val majorityValue = nodeFieldValues.toList.groupBy(_._2).mapValues(_.length).toList.sortBy(-_._2).head._1

      nodeFieldValues.toList.filter( _._2 != majorityValue).map { case (node: String, value: T) => {
        s"Node $node has value $value for field $fieldName while the majority of nodes have value $majorityValue"
      } }
    }
  }
}
