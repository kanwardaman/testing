package org.example

import com.github.takezoe.retry.{ExponentialBackOff, RetryPolicy, retry}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import scalaj.http.{Http, HttpOptions, HttpResponse}

import java.io.File
import java.sql.Timestamp
import scala.concurrent.duration.DurationInt

/*
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm;*/
object Api {
  val logger = Logger("Api")
  //val creds = ConfigFactory.parseFile(new File("./configs/zoom_credentials.conf"))
val conf= AppConfig

  def getApiPostResponse(url: String, token: String = "", postData: String = ""): (Timestamp, Long, HttpResponse[String]) = {
    implicit val policy = RetryPolicy(
      maxAttempts = conf.api.max_retries,
      retryDuration = conf.api.retryBackOff.millisecond,
      backOff = ExponentialBackOff // default is FixedBackOff
    )

    var attempt = 0
    val startTime = System.currentTimeMillis()
    val response: HttpResponse[String] = if (token == "token-generation-error") {
      logger.warn(s"Empty Token given for url[$url] with postData[$postData]")
      HttpResponse[String]("Failed to get Token", 433, Map.empty[String, IndexedSeq[String]])
    } else {
      try {
        retry {
          attempt += 1
          if (attempt == 2)      logger.info(s"Attempt[${attempt}] for URL[${url}] with payload[$postData]")
          else if (attempt >= 3) logger.warn(s"Attempt[${attempt}] for URL[${url}] with payload[$postData]")

          Http(url)
            .header("Content-Type", "application/json; charset=utf-8")
            .header("Accept", "application/json")
            .header("x-redlock-auth", token)
            .option(HttpOptions.readTimeout(conf.api.readTimeout))
            .postData(postData)
            .method("post")
            .execute()
        }
      } catch {
        case e: Throwable => HttpResponse[String](e.getMessage, 434, Map.empty[String, IndexedSeq[String]])
      }
    }
    if (!response.isSuccess) {
      logger.error(s"Failed on [$url] postData[$postData] -- ResponseCode[${response.code}] -- Response[${response.body}]")
    }
    val responseTime = System.currentTimeMillis() - startTime
    val responseTimestamp = new Timestamp(System.currentTimeMillis())
    (responseTimestamp, responseTime, response)
  }

  def getApiGetResponse(url: String, token: String= ""): (Timestamp, Long, HttpResponse[String]) = {
    implicit val policy = RetryPolicy(
      maxAttempts = conf.api.max_retries,
      retryDuration = conf.api.retryBackOff.millisecond,
      backOff = ExponentialBackOff // default is FixedBackOff
    )
    val startTime = System.currentTimeMillis()
    val response: HttpResponse[String] = retry {
      Http(url)
        .header("Content-Type", "application/json; charset=utf-8")
        .header("Accept", "application/json")
        .header("Authorization", token)
        .option(HttpOptions.readTimeout(conf.api.readTimeout))
        .method("get")
        .execute()
    }

    if (!response.isSuccess) {
      logger.error(s"Failed on $url ResponseCode: ${response.code} -- Response: ${response.body}")
      throw new Exception(s"Failed on $url ResponseCode (${response.code})")
    }

    val responseTime = System.currentTimeMillis() - startTime
    val responseTimestamp = new Timestamp(System.currentTimeMillis())
    (responseTimestamp, responseTime, response)
  }
/*

  def  createJWTToken()= {
    //The JWT signature algorithm we will be using to sign the token
    val  signatureAlgorithm = SignatureAlgorithm.HS256;
    val nowMillis = System.currentTimeMillis();
    val now = new Date(nowMillis);
    val ttlMillis=6000000

      val builder = Jwts.builder()
      .setHeaderParam("typ", "JWT")
      .setHeaderParam("alg", "HS256")
        .setIssuedAt(now)
        .setIssuer(AppConfig.zoomCred.api_key)
        .signWith(signatureAlgorithm, AppConfig.zoomCred.secret_key.getBytes("UTF-8"));
      //if it has been specified, let's add the expiration
      if (ttlMillis >= 0) {
        val expMillis = nowMillis + ttlMillis;
        val exp = new Date(expMillis);
        builder.setExpiration(exp);
      }

     builder.compact()
    //Builds the JWT and serializes it to a compact, URL-safe string
  }
*/

/*
  def getToken( token: String, tokenTime: Long): (String, Long) = {
//To Do Modify according to zoom api
    if (token == null || token == "" || (System.currentTimeMillis - tokenTime) > 180000) {
      val url = creds.getString(s"credentials.zoom.base_url")
      implicit val formats = DefaultFormats

      val u = new String(Base64.decode(creds.getString(s"credentials.encoded_username")), "UTF-8")
      val p = new String(Base64.decode(creds.getString(s"credentials.encoded_password")), "UTF-8")
      println(u,p)

      val loginCreds = LoginCreds(u, p,   creds.getString(s"credentials.customer_name"))

      val response = getApiPostResponse(url, token = "", postData = write(loginCreds))._3
      if (!response.isSuccess || response.body.length<5) {
        logger.error(s"Failed to get token")
        ("token-generation-error", 0)
      } else {
        val parsedToken = (parse(response.body) \\ "token").values.toString
        (parsedToken, System.currentTimeMillis)
      }
    } else {
      (token, tokenTime)
    }
  }*/
}