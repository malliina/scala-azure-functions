val Azure = config("azure")
val build = taskKey[Unit]("builds")

val circeModules = Seq("circe-generic", "circe-parser")
val logbackModules = Seq("classic", "core")

val saf = project
  .in(file("."))
  .settings(
    version := "0.0.1",
    scalaVersion := "3.1.1",
    crossScalaVersions := Seq(scalaVersion.value),
    libraryDependencies ++=
      logbackModules.map(m => "ch.qos.logback" % s"logback-$m" % "1.2.10") ++
      circeModules.map(m => "io.circe" %% m % "0.14.1") ++
      Seq(
        "com.microsoft.azure.functions" % "azure-functions-java-library" % "1.4.2",
        "org.scalameta" %% "munit" % "0.7.29" % Test
      ),
    testFrameworks += new TestFramework("munit.Framework"),
    Azure / target := target.value / "azure",
    build := Def.taskDyn {
      val azureTarget = (Azure / target).value
      (Compile / run).toTask(s" $azureTarget ${assembly.value}")
    }.value,
    build := build.dependsOn(assembly).value
  )

Global / onChangedBuildSource := ReloadOnSourceChanges
