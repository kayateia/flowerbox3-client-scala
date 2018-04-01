package net.kayateia.flowerbox.client

import org.lwjgl.opengl.GL11._

import scala.collection.mutable.ArrayBuffer

object Renderer {
	def setup() {
		genLand()
	}

	private val cubeMap = new Array[Int](16*16*16)
	private def coord(x: Int, y: Int, z: Int) = z*16*16 + y*16 + x

	private def genLand() {
		for (x <- 0 to 15; z <- 0 to 15) {
			val height = Math.floor(Math.random() * 15).asInstanceOf[Int]
			for (y <- 0 to height)
				cubeMap(coord(x, y, z)) = 1
		}
		genDL()
	}

	case class Coord(x: Int, y: Int, z: Int,
			top: Boolean = true, left: Boolean = true, right: Boolean = true, bottom: Boolean = true,
			front: Boolean = true, back: Boolean = true) {
		def isEmpty = !top && !left && !right && !bottom && !front && !back
	}

	private val displayList = ArrayBuffer[Coord]()

	private def genDL() {
		for (x <- 0 to 15; y <- 0 to 15; z <- 0 to 15) {
			def isFilled(x: Int, y: Int, z: Int) = cubeMap(coord(x, y, z)) > 0
			if (isFilled(x, y, z)) {
				def onEdge(c: Int) = c == 0 || c == 15
				def isEdgeBlock(x: Int, y: Int, z: Int) = onEdge(x) || onEdge(y) || onEdge(z)
				if (isEdgeBlock(x, y, z)) {
					displayList += Coord(x, y, z)
				} else {
					val coord = Coord(x, y, z,
						!isFilled(x, y + 1, z),
						!isFilled(x - 1, y, z),
						!isFilled(x + 1, y, z),
						!isFilled(x, y - 1, z),
						!isFilled(x, y, z - 1),
						!isFilled(x, y, z + 1)
					)
					if (!coord.isEmpty)
						displayList += coord
				}
			}
		}
		println(displayList.length, displayList)
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

	def oneCube(xcen: Float, ycen: Float, zcen: Float, coord: Coord) {
		def alpha = 0.5f
		glPushMatrix()
		glTranslatef(xcen, ycen, zcen)
		glBegin(GL_QUADS)
		if (coord.top) {
			glColor4f(1.0f,1.0f,0.0f, alpha)
			glVertex3f( 1.0f, 1.0f,-1.0f)
			glVertex3f(-1.0f, 1.0f,-1.0f)
			glVertex3f(-1.0f, 1.0f, 1.0f)
			glVertex3f( 1.0f, 1.0f, 1.0f)
		}
		if (coord.bottom) {
			glColor4f(1.0f,0.5f,0.0f, alpha)
			glVertex3f( 1.0f,-1.0f, 1.0f)
			glVertex3f(-1.0f,-1.0f, 1.0f)
			glVertex3f(-1.0f,-1.0f,-1.0f)
			glVertex3f( 1.0f,-1.0f,-1.0f)
		}
		if (coord.back) {
			glColor4f(1.0f,0.0f,0.0f, alpha)
			glVertex3f( 1.0f, 1.0f, 1.0f)
			glVertex3f(-1.0f, 1.0f, 1.0f)
			glVertex3f(-1.0f,-1.0f, 1.0f)
			glVertex3f( 1.0f,-1.0f, 1.0f)
		}
		if (coord.front) {
			glColor4f(1.0f,1.0f,0.0f, alpha)
			glVertex3f( 1.0f,-1.0f,-1.0f)
			glVertex3f(-1.0f,-1.0f,-1.0f)
			glVertex3f(-1.0f, 1.0f,-1.0f)
			glVertex3f( 1.0f, 1.0f,-1.0f)
		}
		if (coord.left) {
			glColor4f(0.0f,0.0f,1.0f, alpha)
			glVertex3f(-1.0f, 1.0f, 1.0f)
			glVertex3f(-1.0f, 1.0f,-1.0f)
			glVertex3f(-1.0f,-1.0f,-1.0f)
			glVertex3f(-1.0f,-1.0f, 1.0f)
		}
		if (coord.right) {
			glColor4f(1.0f,0.0f,1.0f, alpha)
			glVertex3f( 1.0f, 1.0f,-1.0f)
			glVertex3f( 1.0f, 1.0f, 1.0f)
			glVertex3f( 1.0f,-1.0f, 1.0f)
			glVertex3f( 1.0f,-1.0f,-1.0f)
		}
		glEnd()
		glPopMatrix()
	}

	def render(width: Int, height: Int) {
		glViewport(0, 0, width, height)
		glClearColor(0f, 0f, 0.2f, 0f)
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

		glEnable(GL_DEPTH_TEST)
		glDepthFunc(GL_LESS)
		glShadeModel(GL_SMOOTH)
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST)
		glEnable(GL_BLEND)
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

		setupProjectionMatrix(width, height)

		glMatrixMode(GL_MODELVIEW)
		glLoadIdentity()

		glTranslatef(0f, -10.0f, -40f)
		glRotatef(rot, 0.0f, 1.0f, 0.0f)
		glColor3f(0.5f, 0.5f, 1.0f)

		rot = (rot + 0.5f) % 360

		/*for (x <- 0 to 15; y <- 0 to 15; z <- 0 to 15) {
			if (cubeMap(z*256 + y*16 + x) > 0)
				oneCube((x - 8).asInstanceOf[Float], (y - 8).asInstanceOf[Float], (z - 8).asInstanceOf[Float])
		} */
		for (c <- displayList) {
			oneCube((c.x - 8).asInstanceOf[Float], (c.y - 8).asInstanceOf[Float], (c.z - 8).asInstanceOf[Float], c)
		}
	}
}
