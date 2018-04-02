package net.kayateia.flowerbox.client

import org.lwjgl.opengl.GL11._

object Renderer {
	private val chunks = new Array[Chunk](9)

	def setup() {
		// chunk.setup()
		for (x <- 0 to 2; z <- 0 to 2) {
			chunks(x * 3 + z) = new Chunk
			chunks(x * 3 + z).setup()
		}
	}

	private val PI = 3.14159265358979323846
	private def coTangent(angle: Float) = (1f / Math.tan(angle)).toFloat
	private def degreesToRadians(degrees: Float) = degrees * (PI / 180d).asInstanceOf[Float]

	private def setupProjectionMatrix(width: Int, height: Int) {
		// Setup projection matrix
		val fieldOfView = 60f
		val aspectRatio = width.asInstanceOf[Float] / height.asInstanceOf[Float]
		val near_plane = 0.1f
		val far_plane = 100f

		// This code didn't work, but I'm keeping it around for use with vertex shaders later.
		/*val y_scale = coTangent(degreesToRadians(fieldOfView / 2f))
		val x_scale = y_scale / aspectRatio
		val frustum_length = far_plane - near_plane

		// Column-major matrix
		val projectionMatrix: Array[Float] = Array(
			x_scale, 0, 0, 0,
			0, y_scale, 0, 0,
			0, 0, -((far_plane + near_plane) / frustum_length), -1,
			0, 0, -((2 * near_plane * far_plane) / frustum_length), 0
		)

		glMatrixMode(GL_PROJECTION)
		glLoadMatrixf(projectionMatrix) */

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
	}

	private var rot = 0.0f;

	def render(width: Int, height: Int) {
		Textures.loadAll()

		glViewport(0, 0, width, height)
		glClearColor(133f / 255f, 225f / 255f, 1f, 0f)
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

		glEnable(GL_DEPTH_TEST)
		glDepthFunc(GL_LESS)
		glShadeModel(GL_SMOOTH)
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST)
		//glEnable(GL_BLEND)
		//glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

		setupProjectionMatrix(width, height)

		glMatrixMode(GL_MODELVIEW)
		glLoadIdentity()

		glTranslatef(0f, -5.0f, -40f)
		glRotatef(rot, 0.0f, 1.0f, 0.0f)
		glColor3f(0.5f, 0.5f, 1.0f)

		rot = (rot + 0.5f) % 360

		/*for (x <- 0 to 15; y <- 0 to 15; z <- 0 to 15) {
			if (cubeMap(z*256 + y*16 + x) > 0)
				oneCube((x - 8).asInstanceOf[Float], (y - 8).asInstanceOf[Float], (z - 8).asInstanceOf[Float])
		} */
		for (x <- 0 to 2; z <- 0 to 2)
			chunks(x*3 + z).render(-16f + 16f*x, -16f + 16f*z)
	}
}
