package net.pdiaz.flyapp.app

import unfiltered.netty._
import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import net.pdiaz.flyapp.stores.FlightMemStore
import net.pdiaz.flyapp.stores.{ Flight, FlightStore, DataAlreadyExists, StoreDone, DataNotFound, ModifyDone }
import argonaut.integrate.unfiltered._
import argonaut._
import Argonaut._
import scalaz._
import Scalaz._
import com.github.nscala_time.time.Imports._
import scala.language.implicitConversions

//** unfiltered plan */
class FlightPlan(flightStore: FlightStore = new FlightMemStore) extends cycle.Plan with cycle.ThreadPool with ServerErrorResponse {

  flightStore.putDefaultData()

  //  def ensureJSONContentType = ensureContentType("application/json")

  def intent = unfiltered.Cycle.Intent {

    case GET(Path("/flights") & Accepts.Json(_) & Params(pars)) => {
      def toOption = pars.get("to") match {
        case Some(seq) => Some(seq.head)
        case None => None
      }
      def fromOption = pars.get("from") match {
        case Some(seq) => Some(seq.head)
        case None => None
      }

      val data = flightStore.listFilter(toOption, fromOption)

      JsonResponse(data)
    }
    case PUT(Path(Seg(List("flights", key))) & Accepts.Json(_) & Params(pars) & RequestContentType("application/json") & content) => {
      Body.string(content).decodeOption[Flight] match {
        case Some(flight) => {
          flightStore.update(key, flight) match {
            case DataNotFound => {
              NotFound
            }
            case ModifyDone => {
              NoContent
            }
          }
        }
        case None => {
          BadRequest
        }
      }
    }

    case POST(Path(Seg(List("flights", key))) & Accepts.Json(_) & Params(pars) & RequestContentType("application/json") & content) => {
      Body.string(content).decodeOption[Flight] match {
        case Some(flight) => {
          flightStore.store(key, flight) match {
            case DataAlreadyExists => {
              Conflict
            }
            case StoreDone => {
              Created
            }
          }
        }
        case None => {
          BadRequest
        }
      }

    }

    case POST(Path(Seg(List("flights", key))) & Params(pars) & RequestContentType("application/json") & content) => {
      flightStore.delete(key) match {
        case DataNotFound => {
          NotFound
        }
        case ModifyDone => {
          NoContent
        }
      }
    }
  }
}

class HomePagePlan extends cycle.Plan with cycle.ThreadPool with ServerErrorResponse {
  def intent = {
    case Path("/test3") => ResponseString("I'm alive")
    case Path("/") => {
      Html5(
        <html>
          <head><title>Pablo monthly Scala</title></head>
          <body>
            <p>
              Welcome
            </p>
            <p>
              This is under construction but nearly finish, I need to investigate a bit how to test this :)
    		  	To try this API, download chrome postman<a href="https://chrome.google.com/webstore/detail/postman-rest-client-packa/fhbjgbiflinjbdggehcddcbncdddomop">HERE</a>
            </p>
            <p>
              And import<a href="https://www.getpostman.com/collections/cb088452d39f131e4b7f">this</a>
            </p>
            <p>
              If you want to play by yourself here you have the /flights resource at http://127.0.0.1/flights with some data
            </p>
            <ul>
              <li>POST /flights/[id]: To create a new resource path parameter id</li>
              <li>PUT /flights/[id]: To change an already existing resource with path parameter id</li>
              <li>DELETE /flights/[id]: To delete an already existing resource with path parameter id</li>
              <li>GET /flights: To retrieve all the flights within time, also accepts to do queries usign URL params (to,from) to filter the data</li>
            </ul>
            <hr/>
            Pablo D&iacute;az
          </body>
        </html>)
    }
  }
}

object Server {
  def main(args: Array[String]) {
    val hello = unfiltered.netty.cycle.Planify {
      case _ => ResponseString("hello world")
    }

    unfiltered.netty.Http(8080).plan(new HomePagePlan).plan(new FlightPlan).run()
  }
}
