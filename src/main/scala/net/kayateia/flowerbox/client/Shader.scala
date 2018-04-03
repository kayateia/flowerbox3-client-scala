package net.kayateia.flowerbox.client

import org.lwjgl.opengl.GL20._

import scala.io.Source

class Shader(filename: String, shaderType: Int) {
	private val lines = Source.fromFile(filename).getLines.toList
	private val shaderSource = lines.foldLeft("")(_ + _ + "\n")
	val shaderId = glCreateShader(shaderType)
	glShaderSource(shaderId, shaderSource)
	glCompileShader(shaderId)

	val result = new Array[Int](1)
	glGetProgramiv(shaderId, GL_COMPILE_STATUS, result)
	if (result(0) == 0) {
		val logs = glGetShaderInfoLog(shaderId)
		println(logs)
	}
}
