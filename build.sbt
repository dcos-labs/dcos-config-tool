import sbtassembly.MergeStrategy

name := "dcos-config-reader"
organization := "ftrossbach"

version := "1.0"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.4"

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"                    % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j"                    % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit"                  % akkaVersion  % "test",
  "com.typesafe.akka" %% "akka-http-experimental"        % akkaVersion,
  "com.typesafe.akka" %% "akka-http-testkit"             % akkaVersion  % "test",
  "com.lambdaworks"   %% "jacks"                         % "2.5.2",
  "commons-io"        %  "commons-io"                    % "2.5"
)


enablePlugins(DockerPlugin)

dockerfile in docker := {
  // The assembly task generates a fat JAR file
  val artifact: File = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("java")
    add(artifact, artifactTargetPath)
    expose(8080)
    entryPoint("java", "-cp", artifactTargetPath, "de.ftrossbach.dcos.config.reader.Main")
  }
}

imageNames in docker := Seq(
  // Sets the latest tag
  ImageName(s"${organization.value}/${name.value}:latest")
)

buildOptions in docker := BuildOptions(cache = false)