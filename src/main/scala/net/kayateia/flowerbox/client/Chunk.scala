package net.kayateia.flowerbox.client

import com.flowpowered.noise.module.source.Perlin
import org.lwjgl.opengl.GL11._

import scala.collection.mutable.ArrayBuffer

class Chunk {
	def setup() {
		genLand()
	}

	private val cubeMap = new Array[Int](16*16*16)
	private def coord(x: Int, y: Int, z: Int) = z*16*16 + y*16 + x

	private def genLand() {
		val xMin = 2f
		val xMax = 6f
		val xSize = xMax - xMin
		val zMin = 1f
		val zMax = 5f
		val zSize = xMax - zMin
		val perlin = new Perlin()
		for (x <- 0 to 15; z <- 0 to 15) {
			// val height = Math.floor(Math.random() * 15).asInstanceOf[Int]
			val height = Math.floor(perlin.getValue(xMin + xSize*(x / 16f), 0, zMin + zSize*(z / 16f))).asInstanceOf[Int]
			val heightMaxed = 5 + (if (height > 15) 15 else height)
			for (y <- 0 to heightMaxed)
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

	private def oneCube(xcen: Float, ycen: Float, zcen: Float, coord: Coord) {
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

	def render(xOffset: Float, zOffset: Float) {
		for (c <- displayList) {
			oneCube(xOffset + (c.x - 8).asInstanceOf[Float], (c.y - 8).asInstanceOf[Float], zOffset + (c.z - 8).asInstanceOf[Float], c)
		}
	}
}
