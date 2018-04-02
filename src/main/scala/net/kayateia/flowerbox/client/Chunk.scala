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
			val height = Math.floor(8f * perlin.getValue(xMin + xSize*(x / 16f), 0, zMin + zSize*(z / 16f))).asInstanceOf[Int]
			val heightMaxed = 2 + (if (height > 14) 14 else height)
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
		glEnable(GL_TEXTURE_2D)
		glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE)

		// def alpha = 0.5f
		glPushMatrix()
		glTranslatef(xcen, ycen, zcen)
		glScalef(0.5f,0.5f,0.5f)
		glColor4f(1f, 1f, 1f, 1f)
		//glBegin(GL_QUADS)
		if (coord.top) {
			glBindTexture(GL_TEXTURE_2D, Textures.grassTop)
			glBegin(GL_QUADS)
			// glColor4f(1.0f,1.0f,0.0f, alpha)
			glTexCoord2f(1f, 0f)
			glVertex3f( 1.0f, 1.0f,-1.0f)
			glTexCoord2f(0f, 0f)
			glVertex3f(-1.0f, 1.0f,-1.0f)
			glTexCoord2f(0f, 1f)
			glVertex3f(-1.0f, 1.0f, 1.0f)
			glTexCoord2f(1f, 1f)
			glVertex3f( 1.0f, 1.0f, 1.0f)
			glEnd()
		}
		if (coord.bottom) {
			glBindTexture(GL_TEXTURE_2D, Textures.dirt)
			glBegin(GL_QUADS)
			// glColor4f(1.0f,0.5f,0.0f, alpha)
			glTexCoord2f(1f, 0f)
			glVertex3f( 1.0f,-1.0f, 1.0f)
			glTexCoord2f(0f, 0f)
			glVertex3f(-1.0f,-1.0f, 1.0f)
			glTexCoord2f(0f, 1f)
			glVertex3f(-1.0f,-1.0f,-1.0f)
			glTexCoord2f(1f, 1f)
			glVertex3f( 1.0f,-1.0f,-1.0f)
			glEnd()
		}
		if (coord.back) {
			glBindTexture(GL_TEXTURE_2D, Textures.grassSide)
			glBegin(GL_QUADS)
			// glColor4f(1.0f,0.0f,0.0f, alpha)
			glTexCoord2f(1f, 0f)
			glVertex3f( 1.0f, 1.0f, 1.0f)
			glTexCoord2f(0f, 0f)
			glVertex3f(-1.0f, 1.0f, 1.0f)
			glTexCoord2f(0f, 1f)
			glVertex3f(-1.0f,-1.0f, 1.0f)
			glTexCoord2f(1f, 1f)
			glVertex3f( 1.0f,-1.0f, 1.0f)
			glEnd()
		}
		if (coord.front) {
			glBindTexture(GL_TEXTURE_2D, Textures.grassSide)
			glBegin(GL_QUADS)
			// glColor4f(1.0f,1.0f,0.0f, alpha)
			glTexCoord2f(1f, 0f)
			glVertex3f( 1.0f,-1.0f,-1.0f)
			glTexCoord2f(0f, 0f)
			glVertex3f(-1.0f,-1.0f,-1.0f)
			glTexCoord2f(0f, 1f)
			glVertex3f(-1.0f, 1.0f,-1.0f)
			glTexCoord2f(1f, 1f)
			glVertex3f( 1.0f, 1.0f,-1.0f)
			glEnd()
		}
		if (coord.left) {
			glBindTexture(GL_TEXTURE_2D, Textures.grassSide)
			glBegin(GL_QUADS)
			// glColor4f(0.0f,0.0f,1.0f, alpha)
			glTexCoord2f(1f, 0f)
			glVertex3f(-1.0f, 1.0f, 1.0f)
			glTexCoord2f(0f, 0f)
			glVertex3f(-1.0f, 1.0f,-1.0f)
			glTexCoord2f(0f, 1f)
			glVertex3f(-1.0f,-1.0f,-1.0f)
			glTexCoord2f(1f, 1f)
			glVertex3f(-1.0f,-1.0f, 1.0f)
			glEnd()
		}
		if (coord.right) {
			glBindTexture(GL_TEXTURE_2D, Textures.grassSide)
			glBegin(GL_QUADS)
			// glColor4f(1.0f,0.0f,1.0f, alpha)
			glTexCoord2f(1f, 0f)
			glVertex3f( 1.0f, 1.0f,-1.0f)
			glTexCoord2f(0f, 0f)
			glVertex3f( 1.0f, 1.0f, 1.0f)
			glTexCoord2f(0f, 1f)
			glVertex3f( 1.0f,-1.0f, 1.0f)
			glTexCoord2f(1f, 1f)
			glVertex3f( 1.0f,-1.0f,-1.0f)
			glEnd()
		}
		// glEnd()
		glPopMatrix()
	}

	def render(xOffset: Float, zOffset: Float) {
		for (c <- displayList) {
			oneCube(xOffset + (c.x - 8).asInstanceOf[Float], (c.y - 8).asInstanceOf[Float], zOffset + (c.z - 8).asInstanceOf[Float], c)
		}
	}
}
