val zioVersion            = "2.0.13"
val zioJsonVersion        = "0.5.0"
val zioLoggingVersion     = "2.1.12"

val root = (project in file("."))
  .settings(
    inThisBuild(
      List(
        organization := "net.degoes",
        scalaVersion := "2.13.10"
      )
    ),
    name           := "jvm-perf",
    kotlincOptions ++= Seq("-jvm-target", "1.8"),
    libraryDependencies ++= Seq(
      "org.openjdk.jmh" % "jmh-core" % "1.32",
      "io.vavr" % "vavr" % "0.10.4",
      // general
      "dev.zio"        %% "zio-json"            % zioJsonVersion,
      "dev.zio"        %% "zio"                 % zioVersion,

      // logging
      "dev.zio"             %% "zio-logging"       % zioLoggingVersion,
      "dev.zio"             %% "zio-logging-slf4j" % zioLoggingVersion,

      // Kotlin standard library
      "org.jetbrains.kotlin" % "kotlin-stdlib" % kotlinVersion.value,

      // test
      "dev.zio"            %% "zio-test-sbt"                    % zioVersion            % Test,
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
  )
  .enablePlugins(JmhPlugin)
