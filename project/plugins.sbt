addSbtPlugin("ch.epfl.lamp"                % "sbt-dotty"           % "0.4.6")
addSbtPlugin("ch.epfl.scala"               % "sbt-scalajs-bundler" % "0.20.0")
addSbtPlugin("com.eed3si9n"                % "sbt-assembly"        % "0.14.10")
addSbtPlugin("com.eed3si9n"                % "sbt-buildinfo"       % "0.9.0")
addSbtPlugin("com.lightbend.sbt"           % "sbt-javaagent"       % "0.1.6")
addSbtPlugin("com.typesafe.sbt"            % "sbt-git"             % "1.0.0")
addSbtPlugin("com.typesafe.sbt"            % "sbt-native-packager" % "1.7.6")
addSbtPlugin("io.spray"                    % "sbt-revolver"        % "0.9.1")
//addSbtPlugin("org.jmotor.sbt"              % "sbt-dependency-updates" % "1.2.2")
addSbtPlugin("org.scala-js"                % "sbt-scalajs"         % "1.3.1")
addSbtPlugin("org.scalameta"               % "sbt-mdoc"            % "2.2.13")
addSbtPlugin("org.scalameta"               % "sbt-scalafmt"        % "2.4.2")
addSbtPlugin("org.scoverage"               % "sbt-scoverage"       % "1.6.1")
addSbtPlugin("pl.project13.scala"          % "sbt-jmh"             % "0.4.0")
resolvers += Resolver.bintrayRepo("oyvindberg", "converter")
addSbtPlugin("org.scalablytyped.converter" % "sbt-converter"       % "1.0.0-beta28")
