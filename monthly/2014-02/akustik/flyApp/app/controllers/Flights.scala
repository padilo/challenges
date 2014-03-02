package controllers

import scala.concurrent.Future

import model.Flight
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsResult
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller
import repositories.FlightsRepository

class Flights(flightsRepo: FlightsRepository) extends Controller {

  implicit val flightReads = Json.reads[Flight]
  implicit val flightWrites = Json.writes[Flight]

  private val responseSuccess = Ok(Json.obj("result" -> "success"))
  private val responseFailure = Ok(Json.obj("result" -> "failure"))
  private val responseJsonError = BadRequest("json parse error")
  private val responseFlights = (flights: Iterable[Flight]) => Ok(Json.toJson(flights))

  private def validateAndExecuteAsync[T](validate: (JsValue) => JsResult[T],
    operate: (T) => Future[Boolean]) = Action.async(parse.json) {
    request =>
      {
        validate(request.body).map {
          in => operate(in).map(_ match {
            case true => responseSuccess
            case false => responseFailure
          })
        }.recoverTotal {
          e =>
            {
              Logger.error(e.toString)
              Future{responseJsonError}
            }
        }
      }
  }

  def create() = validateAndExecuteAsync(_.validate[Flight], 
    (f: Flight) => flightsRepo.insert(f))

  def to(id: String, from: Long, to: Long) = Action.async {
    flightsRepo.flightsTo(id, from, to).flatMap(flights => Future { 
      Ok(Json.toJson(flights))
    })
  }

  def from(id: String, from: Long, to: Long) = Action.async {
    flightsRepo.flightsFrom(id, from, to).flatMap(flights => Future { 
      Ok(Json.toJson(flights))
    })
  }

  def delete(id: String) = validateAndExecuteAsync(_.validate[String],
    (body: String) => flightsRepo.delete(id))

  def updateStatus(id: String) = validateAndExecuteAsync(_.validate[String], (status: String) => {
    flightsRepo.updateStatus(id, status).map(r => r.foldRight(true)((a, b) => a && b))
  })
}
