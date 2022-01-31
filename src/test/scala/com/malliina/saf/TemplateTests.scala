package com.malliina.saf

import io.circe.*
import io.circe.syntax.EncoderOps

class TemplateTests extends munit.FunSuite:
  test("can run test") {
    assertEquals(1, 1)
  }

  test("serialize") {
    val model = FunctionJson(
      "script",
      "entry",
      Seq(
        Binding(
          BindingType.Http,
          Direction.In,
          "binding",
          Option(Seq(Method.GET)),
          Option(AuthLevel.Anonymous)
        ),
        Binding.outHttp
      )
    )
    println(model.asJson)
  }
