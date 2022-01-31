package com.malliina.saf

import io.circe.*
import io.circe.syntax.EncoderOps
import org.slf4j.LoggerFactory
import scala.quoted.*
import java.nio.file.{Files, Path, Paths, StandardCopyOption, StandardOpenOption}

object FunctionBuilder:
  val log = AppLogger(getClass)

  def main(args: Array[String]): Unit =
    args.toList match
      case dir :: jar :: Nil =>
        packageFunction(Paths.get(dir), Paths.get(jar))
      case other =>
        val msg = s"Expected three parameters, got ${other.length}: ${other.mkString(", ")}"
        throw new IllegalArgumentException(msg)

  def packageFunction(targetDir: Path, functionJar: Path): Unit =
    log.info(s"Packaging function...")
    Files.createDirectories(targetDir)
    val distJar = targetDir.resolve(functionJar.getFileName)
    Files.copy(functionJar, distJar, StandardCopyOption.REPLACE_EXISTING)
    log.info(s"Copied $functionJar to $distJar.")
    val entryClass = classOf[HelloFunction].getName
    val entryMethod = HelloFunction.entryMethod
    val jsonDir = targetDir.resolve(HelloFunction.name)
    Files.createDirectories(jsonDir)
    val relativeJar = jsonDir.relativize(distJar)
    val json = functionJson(HelloFunction.triggerName, relativeJar, s"$entryClass.$entryMethod")
    val functionJsonFile = jsonDir.resolve("function.json")
    Files.writeString(
      functionJsonFile,
      json.asJson.spaces2,
      StandardOpenOption.TRUNCATE_EXISTING,
      StandardOpenOption.CREATE
    )
    log.info(s"Wrote $functionJsonFile.")
    Files.copy(
      getClass.getResourceAsStream("host.json"),
      targetDir.resolve("host.json"),
      StandardCopyOption.REPLACE_EXISTING
    )
    Files.copy(
      getClass.getResourceAsStream("local.settings.json"),
      targetDir.resolve("local.settings.json"),
      StandardCopyOption.REPLACE_EXISTING
    )
    Zip.zipFolder(targetDir)

  def functionJson(triggerName: String, functionJarPath: Path, entryPoint: String): FunctionJson =
    FunctionJson(
      functionJarPath.toString.replace('\\', '/'),
      entryPoint,
      Seq(
        Binding(
          BindingType.HttpTrigger,
          Direction.In,
          triggerName,
          Option(Seq(Method.GET)),
          Option(AuthLevel.Anonymous)
        ),
        Binding.outHttp
      )
    )
