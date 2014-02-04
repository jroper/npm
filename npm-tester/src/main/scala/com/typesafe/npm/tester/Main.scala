package com.typesafe.npm.tester

import akka.actor.ActorSystem

import com.typesafe.jse.{Trireme, Node}
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import com.typesafe.npm.{NpmLoader, Npm}
import java.io.File

object Main {
  def main(args: Array[String]) {
    implicit val system = ActorSystem("npm-system")
    implicit val timeout = Timeout(30.seconds)

    system.scheduler.scheduleOnce(32.seconds) {
      system.shutdown()
      System.exit(1)
    }

    val engine = system.actorOf(Node.props(), "engine")
    val to = new File(new File("target"), "webjars")
    val cacheFile = new File(to, "extraction-cache")
    val npm = new Npm(engine, NpmLoader.load(to, cacheFile, Main.getClass.getClassLoader))
    for (
      result <- npm.update()
    ) yield {
      println(s"status\n======\n${result.exitValue}\n")
      println(s"output\n======\n${new String(result.output.toArray, "UTF-8")}\n")
      println(s"error\n=====\n${new String(result.error.toArray, "UTF-8")}\n")

      try {
        system.shutdown()
        System.exit(0)
      } catch {
        case _: Throwable =>
      }

    }

  }
}
