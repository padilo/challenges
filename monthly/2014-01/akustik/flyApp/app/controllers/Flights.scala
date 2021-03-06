package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import views._
import play.api.libs.json._
import model.Flight
import repositories.FlightsRepository

class Flights(flightsRepo: FlightsRepository) extends Controller {

  implicit val flightReads = Json.reads[Flight]
  implicit val flightWrites = Json.writes[Flight]

  private val responseSuccess = Ok(Json.obj("result" -> "success"))
  private val responseFailure = Ok(Json.obj("result" -> "failure"))
  private val responseJsonError = BadRequest("json parse error")
  private val responseFlights = (flights: Iterable[Flight]) => Ok(Json.toJson(flights))

  private def validateAndExecute[T](validate: (JsValue) => JsResult[T],
    operate: (T) => Unit) = Action(parse.json) {
    request =>
      {
        validate(request.body).map {
          in =>
            {
              try {
                operate(in)
                responseSuccess
              } catch {
                case e: Throwable => {
                  Logger.error(e.toString())
                  responseFailure
                }
              }

            }
        }.recoverTotal {
          e =>
            {
              Logger.error(e.toString)
              responseJsonError
            }
        }
      }
  }

  private def list(block: => Iterable[Flight]) = Action {
    try {
      responseFlights(block)
    } catch {
      case e: Throwable => {
        Logger.error(e.toString())
        responseFailure
      }
    }
  }

  private def execute(block: => Unit) = Action {
    try {
      block
      responseSuccess
    } catch {
      case e: Throwable => {
        Logger.error(e.toString())
        responseFailure
      }
    }
  }

  def create() = {
    validateAndExecute(_.validate[Flight], (f: Flight) => flightsRepo.insert(f))
  }

  def to(id: String, from: Long, to: Long) = list {
    flightsRepo.flightsTo(id, from, to)
  }

  def from(id: String, from: Long, to: Long) = list {
    flightsRepo.flightsFrom(id, from, to)
  }

  def delete(id: String) = execute {
    flightsRepo.delete(id)
  }

  def updateStatus(id: String) = validateAndExecute(_.validate[String], (status: String) => {
    flightsRepo.updateStatus(id, status)
  })

}
