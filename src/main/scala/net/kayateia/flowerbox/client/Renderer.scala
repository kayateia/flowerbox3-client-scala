package net.kayateia.flowerbox.client

import org.lwjgl.opengl.GL11._

object Renderer {
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
		glViewport(0, 0, width, height)
		glClearColor(0f, 0f, rot / 360.0f, 0f)
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

		glEnable(GL_DEPTH_TEST)
		glDepthFunc(GL_LESS)
		glShadeModel(GL_SMOOTH)
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST)

		setupProjectionMatrix(width, height)

		glMatrixMode(GL_MODELVIEW)
		glLoadIdentity()

		glTranslatef(0f,0.0f,-7f)
		glRotatef(rot,0.0f,1.0f,0.0f)
		glRotatef(-rot, 1.0f, 0f, 0f)
		glColor3f(0.5f,0.5f,1.0f)

		rot = (rot + 1) % 360

		glBegin(GL_QUADS)
		glColor3f(1.0f,1.0f,0.0f)
		glVertex3f( 1.0f, 1.0f,-1.0f)
		glVertex3f(-1.0f, 1.0f,-1.0f)
		glVertex3f(-1.0f, 1.0f, 1.0f)
		glVertex3f( 1.0f, 1.0f, 1.0f)
		glColor3f(1.0f,0.5f,0.0f)
		glVertex3f( 1.0f,-1.0f, 1.0f)
		glVertex3f(-1.0f,-1.0f, 1.0f)
		glVertex3f(-1.0f,-1.0f,-1.0f)
		glVertex3f( 1.0f,-1.0f,-1.0f)
		glColor3f(1.0f,0.0f,0.0f)
		glVertex3f( 1.0f, 1.0f, 1.0f)
		glVertex3f(-1.0f, 1.0f, 1.0f)
		glVertex3f(-1.0f,-1.0f, 1.0f)
		glVertex3f( 1.0f,-1.0f, 1.0f)
		glColor3f(1.0f,1.0f,0.0f)
		glVertex3f( 1.0f,-1.0f,-1.0f)
		glVertex3f(-1.0f,-1.0f,-1.0f)
		glVertex3f(-1.0f, 1.0f,-1.0f)
		glVertex3f( 1.0f, 1.0f,-1.0f)
		glColor3f(0.0f,0.0f,1.0f)
		glVertex3f(-1.0f, 1.0f, 1.0f)
		glVertex3f(-1.0f, 1.0f,-1.0f)
		glVertex3f(-1.0f,-1.0f,-1.0f)
		glVertex3f(-1.0f,-1.0f, 1.0f)
		glColor3f(1.0f,0.0f,1.0f)
		glVertex3f( 1.0f, 1.0f,-1.0f)
		glVertex3f( 1.0f, 1.0f, 1.0f)
		glVertex3f( 1.0f,-1.0f, 1.0f)
		glVertex3f( 1.0f,-1.0f,-1.0f)
		glEnd()
	}
}
