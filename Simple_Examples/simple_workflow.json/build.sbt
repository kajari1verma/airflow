name := "%s"
version := "1.0"
scalaVersion in ThisBuild := "2.11.11"
exportJars in ThisBuild := true

val sparkVersion  = "2.2.0.cloudera2"
val hadoopVersion = "2.6.0-cdh5.14.4"


lazy val confgen = project.settings(
  libraryDependencies ++= Seq(
    "org.scalameta" %% "scalameta"      % "1.8.0",
    "org.json4s"    %% "json4s-native"  % "3.5.3",
    "org.json4s"    %% "json4s-jackson" % "3.5.3"
  )
)

lazy val graph = project.settings(
  resolvers ++= Seq(
    "Cloudera"                 at "https://repository.cloudera.com/artifactory/cloudera-repos/",
    Resolver.jcenterRepo
  ),
  libraryDependencies ++= Seq(
    "org.apache.spark"  %% "spark-core"     % sparkVersion % "provided",
    "org.apache.spark"  %% "spark-sql"      % sparkVersion % "provided",
    "org.apache.spark"  %% "spark-hive"     % sparkVersion % "provided"
  ),
  javaOptions in ThisBuild ++= Seq(
    "-Xms1024M",
    "-Xmx4096M",
    "-Xss2m",
    "-XX:MaxPermSize=1024M",
    "-XX:ReservedCodeCacheSize=256M",
    "-XX:+TieredCompilation",
    "-XX:+CMSPermGenSweepingEnabled",
    "-XX:+CMSClassUnloadingEnabled",
    "-XX:+UseConcMarkSweepGC",
    "-XX:+HeapDumpOnOutOfMemoryError"
  ),
  autoScalaLibrary := false,

  assemblyJarName in assembly := "%s.jar",

  assemblyMergeStrategy in assembly <<= (assemblyMergeStrategy in assembly) {
    old => {
      case PathList(ps @ _*) if ps.last.endsWith(".xml") ⇒ MergeStrategy.discard
      case PathList(ps @ _*) if ps.last.endsWith("log4j.properties") ⇒ MergeStrategy.discard
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  },

  scalacOptions in ThisBuild ++= Seq(
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:existentials",
    "-language:reflectiveCalls",
    "-target:jvm-1.8",
    "-Xlint", // Scala 2.11.x only
    "-deprecation",            // Emit warning and location for usages of deprecated APIs.
    "-feature",                // Emit warning and location for usages of features that should be imported explicitly.
    "-unchecked",              // Enable additional warnings where generated code depends on assumptions.
    //    "-Xfatal-warnings",        // Fail the compilation if there are any warnings.
    "-Xlint",                  // Enable recommended additional warnings.
    "-Ywarn-adapted-args",     // Warn if an argument list is modified to match the receiver.
    "-Ywarn-dead-code",        // Warn when dead code is identified.
    "-Ywarn-inaccessible",     // Warn about inaccessible types in method signatures.
    "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
    "-Ywarn-numeric-widen"     // Warn when numerics are widened.
  ),

  javacOptions in ThisBuild ++= Seq(
    "-Werror",
    "-source", "1.8",
    "-target", "1.8"
  ),

  sourceGenerators in Compile += Def.taskDyn {
    val srcJson = "conf/test.json"
    val outFile = sourceManaged.in(Compile).value / "confgen" / "Config.scala"
    Def.task {
      (run in confgen in Compile)
        .toTask(" " + srcJson + " " + outFile.getAbsolutePath)
        .value
      Seq(outFile)
    }
  }.taskValue
)
