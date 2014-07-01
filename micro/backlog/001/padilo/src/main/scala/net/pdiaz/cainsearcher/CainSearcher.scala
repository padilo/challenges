package net.pdiaz.cainsearcher

import java.io.FileInputStream
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter

import _root_.akka.actor._
import _root_.akka.routing.RoundRobinPool

import scala.collection.immutable.{HashSet, Queue}
import scala.io.Source


/**
 * Created by Pablo Diaz on 30/06/2014.
 */
case class QueuedMessage(sender: ActorRef, msg: Message)

class PasswordEncoder extends Actor with ActorLogging{

  def hashAlgorithm = "SHA-256"
  def encoding = "UTF-8"
  val messageDigest = MessageDigest.getInstance(hashAlgorithm)

  def toBase64(arr: Array[Byte]) = DatatypeConverter.printBase64Binary(arr)
  def encode(arr: Array[Byte]) = messageDigest.digest(arr)


  override def receive = {
    case PlainPassword(pass) => sender ! HashedPassword(pass, hash(pass))
  }

  def hash(string:String) = toBase64(encode(string getBytes encoding))
}

class CainSearcher(cainFile: String) extends Actor with ActorLogging {

  val encoderRef = context.actorOf(RoundRobinPool(50).props(Props[PasswordEncoder]), "encoder")
  context.watch(encoderRef)
  val nLines = Util.countLines(cainIS)
  var testQueue = Queue.empty[QueuedMessage]

  def cainIS = new FileInputStream(cainFile)

  override def preStart() = {
    import scala.async.Async.async
    import scala.concurrent.ExecutionContext.Implicits.global

    async {
      Source.fromInputStream(cainIS).getLines().foreach(line => {
        encoderRef ! PlainPassword(line)
      })
    }
  }

  override def receive = readingCain(HashSet(), nLines)

  def working(dict: HashSet[String]): Receive = {
    case TestPassword(user, pass)  => {
      if(dict contains pass) sender ! PasswordIsWeak(user, pass)
      else sender ! PasswordIsStrong(user, pass)
    }
  }

  def readingCain(dict:HashSet[String], nLines:Int): Receive = {
    case HashedPassword(_, pass) => {
      if(nLines > 1) context.become(readingCain(dict + pass, nLines-1), discardOld=true)
      else {
        testQueue.foreach(m => self.tell(m.msg, m.sender))
        testQueue = Queue.empty[QueuedMessage]

        context.unwatch(encoderRef)
        context.stop(encoderRef)

        context.become(working(dict))
      }
    }
    case m:Message => {
      testQueue = testQueue.enqueue(QueuedMessage(sender, m))
    }
  }

}
