package com.trueconnectivity.mandrill

import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import com.typesafe.config.Config
import shapeless._
import spray.httpx.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
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

  /**
   * Custom directive that checks the generated signature of the request with the one passed in the header.
   */
  def mandrillAuthentication : Directive0 = {
    formField(mandrillParamKey).flatMap[HNil] { paramValue =>
      headerValueByName(headerSignatureKey).flatMap[HNil]{ signature =>
        if (authenticate(signature, paramValue)) pass
        else reject(AuthorizationFailedRejection)
      } & cancelAllRejections(ofType[AuthorizationFailedRejection.type])
    }
  }


  val mandrillRoute = path("email") {
    post {
      mandrillAuthentication{
        entity(as[MEvent]) { mevent ⇒
          complete {
            println("OOOOH YEAH " + mevent)
            "Cool"
          }
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
