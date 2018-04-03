package net.kayateia.flowerbox.client

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL20._
import org.lwjgl.util.vector._

object Renderer {
	private val chunks = new Array[Chunk](9)

	private var width: Int = 0
	private var height: Int = 0

	def setup(w: Int, h: Int) {
		width = w
		height = h

		Textures.loadAll()
		setupMatrices()
		setupShaders()

		for (x <- 0 to 2; z <- 0 to 2) {
			chunks(x * 3 + z) = new Chunk(x + Chunk.xSize, z + Chunk.zSize)
			chunks(x * 3 + z).setup()
		}

		chunks(4).createVertexArrays()
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
		val far_plane = 100f

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

	/*private def setupProjectionMatrix(width: Int, height: Int) {
		// Setup projection matrix
		val fieldOfView = 60f
		val aspectRatio = width.asInstanceOf[Float] / height.asInstanceOf[Float]
		val near_plane = 0.1f
		val far_plane = 100f

		glMatrixMode(GL_PROJECTION)
		glLoadIdentity()
		def gluPerspective(fovy: Float, aspect: Float, near: Float, far: Float): Unit = {
			val bottom = -near * Math.tan(fovy / 2).toFloat
			val top = -bottom
			val left = aspect * bottom
			val right = -left
			glFrustum(left, right, bottom, top, near, far)
		}
		gluPerspective(degreesToRadians(fieldOfView), aspectRatio, near_plane, far_plane)
	} */

	var pId: Int = 0
	var projectionMatrixLocation: Int = 0
	var viewMatrixLocation: Int = 0
	var modelMatrixLocation: Int = 0

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
		// Color information will be attribute 1
		// glBindAttribLocation(pId, 1, "in_Color")
		// Textute information will be attribute 2
		glBindAttribLocation(pId, 1, "in_TextureCoord")

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
		var err = glGetError
		viewMatrixLocation = glGetUniformLocation(pId, "viewMatrix")
		err = glGetError
		modelMatrixLocation = glGetUniformLocation(pId, "modelMatrix")
		err = glGetError

		// this.exitOnGLError("setupShaders");
	}


	private var rot = 0.0f;

	def render() {
		glViewport(0, 0, width, height)
		glClearColor(133f / 255f, 225f / 255f, 1f, 0f)
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

		glEnable(GL_DEPTH_TEST)
		glDepthFunc(GL_LESS)
		//glShadeModel(GL_SMOOTH)
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST)
		//glEnable(GL_BLEND)
		//glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

		viewMatrix.setIdentity()
		viewMatrix.translate(new Vector3f(0f, -5f, -40f))
		viewMatrix.rotate(degreesToRadians(rot), new Vector3f(0f, 1, 0f))

		glEnable(GL_TEXTURE_2D)
		glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE)

		glUseProgram(pId)

		projectionMatrix.store(matrix44buffer)
		matrix44buffer.flip()
		glUniformMatrix4fv(projectionMatrixLocation, false, matrix44buffer)
		viewMatrix.store(matrix44buffer)
		matrix44buffer.flip()
		glUniformMatrix4fv(viewMatrixLocation, false, matrix44buffer)
		modelMatrix.store(matrix44buffer)
		matrix44buffer.flip()
		glUniformMatrix4fv(modelMatrixLocation, false, matrix44buffer)

		glUseProgram(0)

		// setupProjectionMatrix(width, height)
		/*glMatrixMode(GL_MODELVIEW)
		glLoadIdentity()
		glTranslatef(0f, -5.0f, -40f)
		glRotatef(rot, 0.0f, 1.0f, 0.0f)
		glColor3f(0.5f, 0.5f, 1.0f) */

		rot = (rot + 0.5f) % 360

		chunks(4).render(pId)

		// chunks(4).buffers.

		//for (x <- 0 to 2; z <- 0 to 2)
		//	chunks(x*3 + z).render(-16f + 16f*x, -16f + 16f*z)
	}
}
