package shine.st.dashboard.actors

import akka.actor.{Actor, ActorRef, Props}

/**
  * Created by shinest on 12/07/2017.
  */

object SummarizeActor {
  def props(out: ActorRef) = Props(new SummarizeActor(out))
}

class SummarizeActor(out: ActorRef) extends Actor {
  var i = 1;
  def receive = {
    case msg: String =>
      out ! ("I received your message: " + msg)
  }
}