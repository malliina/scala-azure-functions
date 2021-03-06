package com.malliina.saf

import com.microsoft.azure.functions.annotation.{FunctionName, HttpTrigger}
import com.microsoft.azure.functions.{ExecutionContext, HttpRequestMessage, HttpResponseMessage, HttpStatus}

import scala.annotation.StaticAnnotation

object HelloFunction:
  final val name = "HelloFunction"
  final val triggerName = "req"
  final val entryMethod = "run"

class HelloFunction:
  @FunctionName(HelloFunction.name)
  def run(
    @HttpTrigger(name = HelloFunction.triggerName)
    request: HttpRequestMessage[Option[String]],
    context: ExecutionContext
  ): HttpResponseMessage =
    val name = Option(request.getQueryParameters.get("name")).getOrElse("unnamed")
    request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build()
