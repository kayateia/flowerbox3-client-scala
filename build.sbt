// Adapted from the scala-lwjgl project, which had this copyright:
/*******************************************************************************
  * Copyright 2015 Serf Productions, LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/


lazy val osName =
	System.getProperty("os.name").split(" ")(0).toLowerCase()

lazy val startOnFirst =
	if (osName == "mac")
		Some("-XstartOnFirstThread")
	else
		None

val lwjglVersion = "3.0.0"


//// Setting up native library extraction ////

ivyConfigurations += config("natives")

lazy val nativeExtractions = SettingKey[Seq[(String, NameFilter, File)]](
	"native-extractions", "(jar name partial, sbt.NameFilter of files to extract, destination directory)"
)

lazy val extractNatives = TaskKey[Unit]("extract-natives", "Extracts native files")


//// Project Configuration ////

name := "flowerbox-client"

version := "0.1"

scalaVersion := "2.12.5"

scalacOptions ++= Seq(
	"-Xlint",
	"-Ywarn-dead-code",
	"-Ywarn-numeric-widen",
	"-Ywarn-unused",
	"-Ywarn-unused-import",
	"-unchecked",
	"-deprecation",
	"-feature",
	"-encoding", "UTF-8",
	"-target:jvm-1.8"
)

javacOptions ++= Seq(
	"-Xlint",
	"-encoding", "UTF-8",
	"-source", "1.8",
	"-target", "1.8"
)

testOptions += Tests.Argument("-oD")

javaOptions ++= {
	val options = List (
		s"-Djava.library.path=${baseDirectory.value}/lib/${osName}"
	)

	startOnFirst map (_ :: options) getOrElse options
}

libraryDependencies ++= Seq(
	"org.scalatest" %% "scalatest"      % "3.0.0"      % "test",
	"org.lwjgl"      % "lwjgl"          % lwjglVersion,
	"org.lwjgl"      % "lwjgl-platform" % lwjglVersion % "natives" classifier "natives-windows",
	"org.lwjgl"      % "lwjgl-platform" % lwjglVersion % "natives" classifier "natives-linux",
	"org.lwjgl"      % "lwjgl-platform" % lwjglVersion % "natives" classifier "natives-osx"
)

nativeExtractions <<= (baseDirectory) { base => Seq (
	("lwjgl-platform-3.0.0-natives-linux.jar",   AllPassFilter, base / "lib/linux"),
	("lwjgl-platform-3.0.0-natives-windows.jar", AllPassFilter, base / "lib/windows"),
	("lwjgl-platform-3.0.0-natives-osx.jar",     AllPassFilter, base / "lib/mac")
)}

extractNatives <<= (nativeExtractions, update) map { (ne, up) =>
	val jars = up.select(configurationFilter("natives"))

	ne foreach { case (jarName, fileFilter, outputPath) =>
		jars find(_.getName.contains(jarName)) map { jar =>
			IO.unzip(jar, outputPath)
		}
	}
}

compile in Compile <<= (compile in Compile) dependsOn (extractNatives)

fork in run := true

cancelable := true

exportJars := true
