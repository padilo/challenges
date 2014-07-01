package net.pdiaz.cainsearcher

import scala.collection.immutable.HashSet

/**
 * Created by pdiaz on 30/06/2014.
 */

trait Message

case class TestPassword(user: String, pass: String) extends Message

case class PasswordIsWeak(user: String, pass: String) extends Message
case class PasswordIsStrong(user: String, pass: String) extends Message

case class Complete(dict: HashSet[String]) extends Message

case class PlainPassword(pass: String) extends Message
case class HashedPassword(original: String, pass: String) extends Message
