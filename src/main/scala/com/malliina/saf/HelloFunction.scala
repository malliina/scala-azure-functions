package com.malliina.saf

import com.microsoft.azure.functions.annotation.{FunctionName, HttpTrigger}
import com.microsoft.azure.functions.{ExecutionContext, HttpRequestMessage, HttpResponseMessage, HttpStatus}

import java.util.Optional
import scala.annotation.StaticAnnotation

object HelloFunction:
  final val name = "HelloFunction"
  final val triggerName = "req"

class HelloFunction:
  @FunctionName(HelloFunction.name)
  def run(
    @HttpTrigger(name = HelloFunction.triggerName)
    request: HttpRequestMessage[Optional[String]],
    context: ExecutionContext
  ): HttpResponseMessage =
    val name = Option(request.getQueryParameters.get("name")).getOrElse("unnamed")
    request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build()
