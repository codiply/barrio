package com.codiply.barrio.helpers

import com.github.nscala_time.time.Imports._

final case class TimeStamp(val millis: Long) extends AnyVal {
  def toDateTime(): DateTime = new DateTime(millis)
  def addMillis(extraMillis: Long): TimeStamp = TimeStamp(millis + extraMillis)
}

object TimeStamp {
  def now(): TimeStamp = TimeStamp(DateTime.now.getMillis)
  def fromDateTime(dateTime: DateTime): TimeStamp = TimeStamp(dateTime.getMillis)
  def fromMillisFromNow(millis: Long): TimeStamp = now.addMillis(millis)
}

object TimeHelper {
  def timeoutFromNowMilliseconds(timeoutOn: TimeStamp): Long =
    DateTime.now.to(timeoutOn.toDateTime()).millis.max(0)

  def slightlyReduceTimeout(timeoutOn: TimeStamp): TimeStamp = {
    val millisNow = DateTime.now.getMillis
    val millisFromNow = Constants.slightlyReduceTimeout(timeoutFromNowMilliseconds(timeoutOn))
    TimeStamp(millisNow + millisFromNow)
  }

  def slightlyIncreaseTimeout(timeoutOn: TimeStamp): TimeStamp = {
    val millisNow = DateTime.now.getMillis
    val millisFromNow = Constants.slightlyIncreaseTimeout(timeoutFromNowMilliseconds(timeoutOn))
    TimeStamp(millisNow + millisFromNow)
  }

  def considerablyReduceTimeout(timeoutOn: TimeStamp): TimeStamp = {
    val millisNow = DateTime.now.getMillis
    val millisFromNow = Constants.considerablyReduceTimeout(timeoutFromNowMilliseconds(timeoutOn))
    TimeStamp(millisNow + millisFromNow)
  }

  def considerablyIncreaseTimeout(timeoutOn: TimeStamp, times: Int = 1): TimeStamp = {
    val millisNow = DateTime.now.getMillis
    val millisFromNow = Constants.considerablyIncreaseTimeout(timeoutFromNowMilliseconds(timeoutOn), times)
    TimeStamp(millisNow + millisFromNow)
  }
}

