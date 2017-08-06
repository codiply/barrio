package com.codiply.barrio.helpers

object VersionHelper {
  val version: String = Option(getClass.getPackage.getImplementationVersion) match {
    case Some(v) => v
    case None => ""
  }
}
