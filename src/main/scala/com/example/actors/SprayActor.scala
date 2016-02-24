package com.example.actors

import akka.actor.Actor
import com.trueconnectivity.mandrill.MandrillWebhookService
import com.typesafe.config.{ConfigFactory, Config}
import spray.http.MediaTypes._
import spray.routing._

class SprayActor extends Actor with DefaultService with MandrillWebhookService {
  val config = ConfigFactory.load()
  def actorRefFactory = context
  def receive = runRoute(defaultRoute ~ mandrillRoute)
}

trait DefaultService extends HttpService {
  val defaultRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Fuck you <i>Dave</i>!</h1>
              </body>
            </html>
          }
        }
      }
    }
}