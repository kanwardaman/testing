package org.example

import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods
import scala.concurrent.duration._

import org.json4s.JArray
import org.json4s.jackson.JsonMethods.parse


object Main extends App {

  implicit val formats = DefaultFormats
  val allURL = AppConfig.urls


  val (response, dataSetName) = createDataset()

  if (response.code == 200) {
    val jobId = getJobIdFromRun(dataSetName)

    var jobStatus = ""

    val deadline = 5.seconds.fromNow

    while (deadline.hasTimeLeft && jobStatus != "FINISHED") {

      jobStatus = getJobIdStatus(jobId)

    }

    val jsonResult = getResult(jobId: String)

  }

  def createDataset() = {

    val createDataSetUrl = allURL.createDataSetUrl
    val payload = AppConfig.createDatasetPayload
    val (responseTimestamp, responseTime, response) = Api.getApiPostResponse(createDataSetUrl, "", payload)
    val dataSetName = (parse(payload) \ "dataset").extract[String]
    (response, dataSetName)
  }

  def getJobIdFromRun(dataSetName: String) = {
    val runURL = allURL.runURL
      .replace("{dataset}", dataSetName)
      .replace("{agentName}", "")
      .replace("{runDate}", "")
    val (responseTimestamp, responseTime, response) = Api.getApiPostResponse(runURL)
    val json = parse(response.body, true)

    (json \ "jobId").extract[String]


  }

  def getJobIdStatus(jobId: String) = {

    val jobIdStatusURL = allURL.jobIdStatusURL
      .replace("{jobId}", jobId)
    val (responseTimestamp, responseTime, response) = Api.getApiGetResponse(jobIdStatusURL)
    val json = parse(response.body, true)
    (json \ "status").extract[String]


  }

  def getResult(jobId: String) = {
    val findingsURL = allURL.findingsURL
      .replace("{jobId}", jobId)
    val (responseTimestamp, responseTime, response) = Api.getApiGetResponse(findingsURL)
    parse(response.body, true)

  }
}
