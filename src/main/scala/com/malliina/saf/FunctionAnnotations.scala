package com.malliina.saf

import scala.quoted.*

// Reads annotations, but unclear how to read annotation parameter values
inline def getAnnotations[A]: List[String] = ${ getAnnotationsImpl[A] }

def getAnnotationsImpl[A: Type](using Quotes): Expr[List[String]] =
  import quotes.reflect.*
  val methods = TypeRepr.of[A].typeSymbol.methodMembers
  val parameterAnnotations =
    methods.flatMap(_.paramSymss.flatten.flatMap(_.annotations))
  val parameterAnnotationNames = parameterAnnotations.map(_.tpe.show)
  val methodAnnotations = methods.flatMap(_.annotations).map(_.tpe.show)
  Expr.ofList(parameterAnnotationNames.map(Expr(_)))
