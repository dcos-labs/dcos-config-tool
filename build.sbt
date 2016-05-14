name := "dcos-config-reader"

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