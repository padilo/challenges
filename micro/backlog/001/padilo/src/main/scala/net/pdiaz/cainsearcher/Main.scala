package net.pdiaz.cainsearcher

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{Future, Await, ExecutionContext}
import scala.io.Source

/**
 * Created by pdiaz on 30/06/2014.
 */
object Main {
  import scala.concurrent.duration._
  import ExecutionContext.Implicits.global
  import scala.language.postfixOps

  def maxDuration = 10.seconds
  implicit def timeout = Timeout(maxDuration)

  val system = ActorSystem("cain-searcher")

  def main (args: Array[String]) {
    val cainFile = if(args.length == 2) args(0) else "data/cain.txt"
    val dataCSV = if(args.length == 2) args(1) else "data/dataset.csv"

    val cain = system.actorOf(Props(new CainSearcher(cainFile)), "cain-searcher")

    Timer.checkpoint

    /*
    Ask for a password, unfortunately the answer of each "ask" maybe not be the correct, but all "asks" will answered
    I feel like Alenjandro Blanco (https://www.youtube.com/watch?v=bJqFHj2JWno)
   */
    val futureResponses = Source.fromFile(dataCSV).getLines().map(line=>{
      val fields = line.split(',')

      cain ? TestPassword(fields(0), fields(1))
    })

    awaitAndPrintResult(cain, futureResponses.toList)

    /*
     FIXME
     This will not be the last line because we are not waiting for the onComplete function of the future akka responses
    */
    Timer.printDiff("CainSearcher complete:")

    system.shutdown()

  }

  def awaitAndPrintResult(cain: ActorRef, futureResponses: List[Future[Any]]) = {
    for {
      futureResponse <- futureResponses
      response <- futureResponse
    } printMessage(response)

    // FIXME: Try to avoid the use of await...
    futureResponses.foreach(Await.result(_, maxDuration))
  }

  def printMessage(msg: Any) =
    msg match {
      case PasswordIsStrong(user, _) => println(s"$user has a strong password")
      case PasswordIsWeak(user, _) => println(s"$user has a weak password")
      case _ => throw new RuntimeException("unexpected message")
    }

}

object Timer {
  var lastCheckpoint = now

  def now = System.currentTimeMillis()

  def checkpoint = {
    val newCheckpoint = now
    val diff = newCheckpoint - lastCheckpoint
    lastCheckpoint = newCheckpoint

    diff
  }

  def printDiff(msg: String="Diff:") {
    println(s"$msg $checkpoint ms")
  }
}

