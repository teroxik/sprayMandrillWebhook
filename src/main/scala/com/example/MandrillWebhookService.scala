package com.trueconnectivity.mandrill

import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import com.typesafe.config.Config
import shapeless._
import spray.http.FormData
import spray.httpx.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsonParser, JsonReader, RootJsonFormat}
import spray.routing._

object MandrillWebhookService {
  val webhookAddressKey = "mandrill.webhook.address"
  val webhookAuthenticationKeyKey = "mandrill.webhook.authKey"
  val headerSignatureKey = "X-Mandrill-Signature"
  val hmacsha1Key = "HmacSHA1"
  val mandrillParamKey = "mandrill_events"
}

case class MEvent(msg : MMsg)
case class MMsg(text : String, attachments : List[MAttachment])
case class MAttachment(name : String, `type` : String, content : String, base64 : Boolean)

trait MandrillWebhookService extends HttpService with DefaultJsonProtocol with SprayJsonSupport {

  implicit val mattachments : RootJsonFormat[MAttachment] = jsonFormat4(MAttachment)
  implicit val meventFormat : RootJsonFormat[MEvent] = jsonFormat1(MEvent)
  implicit val mmsg : RootJsonFormat[MMsg] = jsonFormat2(MMsg)

  import MandrillWebhookService._

  def config : Config

  /**
   * Implementation of the mandrill authentication signature check
   */
  def authenticate(signature : String, paramRaw : String) : Boolean = {
    val url = config.getString(webhookAddressKey)
    val webhookKey = config.getString(webhookAuthenticationKeyKey)
    val signedData = url + mandrillParamKey + paramRaw
    println(signedData)
    //Encoding the signedData using HmacSha1
    val secret = new SecretKeySpec(webhookKey.getBytes, hmacsha1Key)
    val mac = Mac.getInstance(hmacsha1Key)
    mac.init(secret)
    val sha1encrypted = mac.doFinal(signedData.getBytes)

    //Comparing the base64 encoding of that with the signature passed in the header
    val base64encoded = Base64.getEncoder.encodeToString(sha1encrypted)
    println(s"generated $base64encoded")
    println(s"signature $signature")

    base64encoded == signature
  }

  def jsonReaderFor[T](implicit reader : JsonReader[T]) : JsonReader[T] = reader

  /**
   * Custom directive that checks the generated signature of the request with the one passed in the header.
   */
  def mandrillAuthenticated[T](reader : JsonReader[T]) : Directive1[T] = {
    formField(mandrillParamKey).flatMap[T :: HNil] { paramValue =>
      headerValueByName(headerSignatureKey).flatMap[T :: HNil]{ signature =>
        if (authenticate(signature, paramValue)) provide(JsonParser.apply(paramValue).convertTo[T](reader))
        else reject(AuthorizationFailedRejection)
      } & cancelAllRejections(ofType[AuthorizationFailedRejection.type])
    }
  }


  val mandrillRoute = path("email") {
    post {
      mandrillAuthenticated(jsonReaderFor[MEvent]){ mevent =>
        complete {
          println("OOOOH YEAH " + mevent)
          "Cool"
        }
      }
    } ~
    get {
      complete {
        "OK"
      }
    }
  }

}
