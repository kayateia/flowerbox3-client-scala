/*
	Flowerbox
	Copyright 2018 Kayateia

	Permission is hereby granted, free of charge, to any person obtaining a copy of this software
	and associated documentation files (the "Software"), to deal in the Software without
	restriction, including without limitation the rights to use, copy, modify, merge, publish,
	distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
	Software is furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in all copies or
	substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
	BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
	NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
	DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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

package net.kayateia.flowerbox.client

import org.lwjgl._
import glfw._
import opengl._
import Callbacks._
import GLFW._
import org.lwjgl.system.MemoryUtil._

object Main extends App {
	import CallbackHelpers._

	private val WIDTH  = 800
	private val HEIGHT = 600

	def run() {
		try {
			GLFWErrorCallback.createPrint(System.err).set()

			val window = init()
			loop(window)

			glfwFreeCallbacks(window)
			glfwDestroyWindow(window)
		} finally {
			glfwTerminate() // destroys all remaining windows, cursors, etc...
			glfwSetErrorCallback(null).free()
		}
	}

	private def init(): Long = {
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW")

		glfwDefaultWindowHints()
		glfwWindowHint(GLFW_VISIBLE,   GLFW_FALSE) // hiding the window
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE) // window resizing not allowed
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2)
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

		val window = glfwCreateWindow(WIDTH, HEIGHT, "Flowerbox Client", NULL, NULL)
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window")

		glfwSetKeyCallback(window, keyHandler _)

		val vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())

		glfwSetWindowPos (
			window,
			(vidMode. width() -  WIDTH) / 2,
			(vidMode.height() - HEIGHT) / 2
		)

		glfwMakeContextCurrent(window)
		glfwSwapInterval(1)
		glfwShowWindow(window)

		window
	}

	private def loop(window: Long) {
		GL.createCapabilities()

		Renderer.setup(WIDTH, HEIGHT)

		while (!glfwWindowShouldClose(window)) {
			Renderer.render()
			glfwSwapBuffers(window)
			glfwPollEvents()
		}
	}

	private def keyHandler (window: Long, key: Int, scanCode: Int, action: Int, mods: Int): Unit = {
		if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
			glfwSetWindowShouldClose(window, true)
		if (key == GLFW_KEY_LEFT) {
			if (action == GLFW_PRESS)
				Renderer.setRotate(-1f)
			else
				Renderer.setRotate(0f)
		}
		if (key == GLFW_KEY_RIGHT) {
			if (action == GLFW_PRESS)
				Renderer.setRotate(1f)
			else
				Renderer.setRotate(0f)
		}
	}

	run()
}
