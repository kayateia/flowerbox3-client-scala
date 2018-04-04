package net.kayateia.flowerbox.client

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL20._
import org.lwjgl.util.vector._

import scala.collection.mutable

object Renderer {
	// How many chunks we'll render in our small world.
	private val chunkW = 15
	private val chunkH = 15
	private val chunks = new Array[Chunk](chunkW * chunkH)

	// The width/height of the viewport.
	private var width: Int = 0
	private var height: Int = 0

	// Queue of chunks that remain to be turned into VAO/VBOs.
	private val chunkRenderQueue = new mutable.Queue[Chunk]

	// Called once, in the render loop.
	def setup(w: Int, h: Int) {
		width = w
		height = h

		// Pull this once during the setup to make sure it gets loaded.
		println("Atlas ID is " + Textures.atlas.glTextureId)

		setupMatrices()
		setupShaders()

		// Begin background terrain generation.
		new Thread(() => {
			println("Beginning terrain generation")
			for (x <- 0 until chunkW; z <- 0 until chunkH) {
				val newChunk = new Chunk(x + Chunk.xSize, z + Chunk.zSize)
				newChunk.setup()
				newChunk.buffers
				println(s"done with ${x}, ${z}")
				chunkRenderQueue.synchronized {
					chunkRenderQueue.enqueue(newChunk)
				}
				chunks(x * chunkW + z) = newChunk
			}
			println("terrain generation done")
		}).start()
	}

	private val PI = 3.14159265358979323846
	private def coTangent(angle: Float) = (1f / Math.tan(angle)).toFloat
	private def degreesToRadians(degrees: Float) = degrees * (PI / 180d).asInstanceOf[Float]

	private val projectionMatrix = new Matrix4f
	private val viewMatrix = new Matrix4f
	val modelMatrix = new Matrix4f
	private val matrix44buffer = BufferUtils.createFloatBuffer(16)

	private def setupMatrices() {
		val fieldOfView = 60f
		val aspectRatio = width.asInstanceOf[Float] / height.asInstanceOf[Float]
		val near_plane = 0.1f
		val far_plane = 200f

		val y_scale = this.coTangent(this.degreesToRadians(fieldOfView / 2f))
		val x_scale = y_scale / aspectRatio
		val frustum_length = far_plane - near_plane

		projectionMatrix.m00 = x_scale
		projectionMatrix.m11 = y_scale
		projectionMatrix.m22 = -((far_plane + near_plane) / frustum_length)
		projectionMatrix.m23 = -1
		projectionMatrix.m32 = -((2 * near_plane * far_plane) / frustum_length)
		projectionMatrix.m33 = 0
	}

	var pId: Int = 0
	var projectionMatrixLocation: Int = 0
	var viewMatrixLocation: Int = 0
	var modelMatrixLocation: Int = 0
	var lightColorLocation: Int = 0
	var lightPositionLocation: Int = 0
	var ambientLocation: Int = 0

	def setupShaders() {
		// Create a new shader program that links both shaders
		pId = glCreateProgram()

		// Load the vertex shader
		val vsId = new Shader("shaders/basic_vertex.glsl", GL_VERTEX_SHADER)
		// Load the fragment shader
		val fsId = new Shader("shaders/basic_fragment.glsl", GL_FRAGMENT_SHADER)

		glAttachShader(pId, vsId.shaderId)
		glAttachShader(pId, fsId.shaderId)

		// Position information will be attribute 0
		glBindAttribLocation(pId, 0, "in_Position")
		// Normal information will be attribute 1
		glBindAttribLocation(pId, 1, "in_Normal")
		// Texture information will be attribute 2
		glBindAttribLocation(pId, 2, "in_TextureCoord")

		glLinkProgram(pId)
		val result = new Array[Int](1)
		glGetProgramiv(pId, GL_LINK_STATUS, result)
		if (result(0) == 0) {
			val logs = glGetProgramInfoLog(pId)
			println(logs)
		}
		glValidateProgram(pId)

		// Get matrices uniform locations
		projectionMatrixLocation = glGetUniformLocation(pId, "projectionMatrix")
		viewMatrixLocation = glGetUniformLocation(pId, "viewMatrix")
		modelMatrixLocation = glGetUniformLocation(pId, "modelMatrix")

		// Get other parameter locations.
		lightColorLocation = glGetUniformLocation(pId, "lightColor")
		lightPositionLocation = glGetUniformLocation(pId, "lightPos")
		ambientLocation = glGetUniformLocation(pId, "ambientStrength")

		// Set some default lighting.
		glUseProgram(pId)
		glUniform3f(lightColorLocation, 0.5f, 0.5f, 0.5f)
		glUniform3f(lightPositionLocation, 15f, 30f, 15f)
		glUniform1f(ambientLocation, 0.7f)
		glUseProgram(0)

		// this.exitOnGLError("setupShaders");
	}

	private def loadModel() {
		modelMatrix.store(matrix44buffer)
		matrix44buffer.flip()
		glUniformMatrix4fv(modelMatrixLocation, false, matrix44buffer)
	}

	// Camera rotation and delta rotation (for movement).
	private var rot = 0.0f
	private var drot = 0.0f

	def setRotate(d: Float) {
		drot = d
	}

	def render() {
		glViewport(0, 0, width, height)
		glClearColor(183f / 255f, 235f / 255f, 1f, 0f)
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

		glEnable(GL_DEPTH_TEST)
		glDepthFunc(GL_LESS)
		glEnable(GL_CULL_FACE)
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST)
		//glEnable(GL_BLEND)
		//glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

		viewMatrix.setIdentity()
		viewMatrix.rotate(degreesToRadians(20f), new Vector3f(1f, 0f, 0f))
		viewMatrix.translate(new Vector3f(0f, -10f, -50f))
		viewMatrix.rotate(degreesToRadians(rot), new Vector3f(0f, 1, 0f))
		viewMatrix.translate(new Vector3f(-16f/2, 0f, -16f/2))

		glEnable(GL_TEXTURE_2D)
		glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE)

		glUseProgram(pId)

		projectionMatrix.store(matrix44buffer)
		matrix44buffer.flip()
		glUniformMatrix4fv(projectionMatrixLocation, false, matrix44buffer)
		viewMatrix.store(matrix44buffer)
		matrix44buffer.flip()
		glUniformMatrix4fv(viewMatrixLocation, false, matrix44buffer)
		loadModel()

		glUseProgram(0)

		rot = (rot + drot) % 360

		var chunkToRender: Chunk = null
		chunkRenderQueue.synchronized {
			if (!chunkRenderQueue.isEmpty) {
				chunkToRender = chunkRenderQueue.dequeue()
			}
		}
		if (chunkToRender != null)
			chunkToRender.createVertexArrays()

		for (x <- 0 until chunkW; z <- 0 until chunkH) {
			val chunk = chunks(x * chunkW + z)
			if (chunk != null && chunk.vaoId > 0) {
				glUseProgram(pId)
				modelMatrix.setIdentity()
				modelMatrix.translate(new Vector3f(-16f*chunkW/2 + 16f*x, 0f, -16f*chunkH/2 + 16f*z));
				loadModel()
				glUseProgram(0)
				chunks(x*chunkW + z).render(pId)
			}
		}
	}
}
