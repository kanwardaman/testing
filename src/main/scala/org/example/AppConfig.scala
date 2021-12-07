package org.example

import com.typesafe.config.ConfigFactory
import org.json4s.jackson.{Json4sModule, JsonMethods}

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import scala.io.Source

object AppConfig {

  lazy val api = ConfigApi(
    appConfig.getInt("app.api.max_retries"),
    appConfig.getInt("app.api.retry_backoff"),
    appConfig.getInt("app.api.read_timeout"),
    ""
  )
val baseUrl= appConfig.getString("app.url.baseUrl")

  lazy val urls = URLS(
    baseUrl+appConfig.getString("app.url.createDataSetUrl"),
    baseUrl+appConfig.getString("app.url.runURL"),
    baseUrl+appConfig.getString("app.url.jobIdStatusURL"),
    baseUrl+appConfig.getString("app.url.findingsURL")
  )




  val appConfig = ConfigFactory.parseFile(new File("./configs/dev.conf"))
  val creds = ConfigFactory.parseFile(new File("./configs/zoom_credentials.conf"))
  val createDatasetPayload = scala.io.Source.fromFile("datasetPayload.json").getLines.mkString
  val datePartition = new SimpleDateFormat("yyyyMMdd").format(new Date)
  var specifiedMetric = "all"

  case class ConfigApi(max_retries: Int, retryBackOff: Int, readTimeout: Int, regions: String)

  case class URLS(
                   createDataSetUrl:String,
                   runURL:String,
                   jobIdStatusURL:String,
                   findingsURL:String
                 )

}
