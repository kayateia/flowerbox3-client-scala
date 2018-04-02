package net.kayateia.flowerbox.client

import com.flowpowered.noise.module.source.Perlin
import org.lwjgl.opengl.GL11._

import scala.collection.mutable.ArrayBuffer

object Chunk {
	val xSize = 0.25f
	val zSize = 0.25f
}

class Chunk(val globalX: Float, val globalZ: Float) {
	def setup() {
		genLand()
	}

	private val cubeMap = new Array[Int](16*16*16)
	private def coord(x: Int, y: Int, z: Int) = z*16*16 + y*16 + x

	private def genLand() {
		val xMin = globalX * Chunk.xSize
		val zMin = globalZ * Chunk.zSize
		val perlin = new Perlin()
		perlin.setOctaveCount(3)
		perlin.setPersistence(0.3)

		for (x <- 0 until 16; z <- 0 until 16) {
			// val height = Math.floor(Math.random() * 15).asInstanceOf[Int]
			val height = Math.floor(13f * perlin.getValue(xMin + Chunk.xSize*(x / 16f), 0, zMin + Chunk.zSize*(z / 16f))).asInstanceOf[Int]
			val heightMinMaxed = Math.max(Math.min(height, 13), 1)
			for (y <- 0 until heightMinMaxed)
				cubeMap(coord(x, y, z)) = 2
			cubeMap(coord(x, heightMinMaxed, z)) = 1
		}
		genDL()
	}

	case class Voxel(x: Int, y: Int, z: Int, blockType: Int,
					 top: Boolean = true, left: Boolean = true, right: Boolean = true, bottom: Boolean = true,
					 front: Boolean = true, back: Boolean = true) {
		def isEmpty = !top && !left && !right && !bottom && !front && !back
	}

	private val displayList = ArrayBuffer[Voxel]()

	private def genDL() {
		for (x <- 0 until 16; y <- 0 until 16; z <- 0 until 16) {
			def isFilled(x: Int, y: Int, z: Int) = cubeMap(coord(x, y, z)) > 0
			if (isFilled(x, y, z)) {
				def onEdge(c: Int) = c == 0 || c == 15
				def isEdgeBlock(x: Int, y: Int, z: Int) = onEdge(x) || onEdge(y) || onEdge(z)
				if (isEdgeBlock(x, y, z)) {
					displayList += Voxel(x, y, z, cubeMap(coord(x, y, z)))
				} else {
					val voxel = Voxel(x, y, z,
						cubeMap(coord(x, y, z)),
						!isFilled(x, y + 1, z),
						!isFilled(x - 1, y, z),
						!isFilled(x + 1, y, z),
						!isFilled(x, y - 1, z),
						!isFilled(x, y, z - 1),
						!isFilled(x, y, z + 1)
					)
					if (!voxel.isEmpty)
						displayList += voxel
				}
			}
		}
	}

	private def oneCube(xcen: Float, ycen: Float, zcen: Float, voxel: Voxel) {
		glEnable(GL_TEXTURE_2D)
		glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE)

		// def alpha = 0.5f
		glPushMatrix()
		glTranslatef(xcen, ycen, zcen)
		glScalef(0.5f,0.5f,0.5f)
		glColor4f(1f, 1f, 1f, 1f)
		//glBegin(GL_QUADS)
		if (voxel.top) {
			voxel.blockType match {
				case 1 => glBindTexture(GL_TEXTURE_2D, Textures.grassTop)
				case 2 => glBindTexture(GL_TEXTURE_2D, Textures.dirt)
			}
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
		if (voxel.bottom) {
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
		if (voxel.back) {
			voxel.blockType match {
				case 1 => glBindTexture(GL_TEXTURE_2D, Textures.grassSide)
				case 2 => glBindTexture(GL_TEXTURE_2D, Textures.dirt)
			}
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
		if (voxel.front) {
			voxel.blockType match {
				case 1 => glBindTexture(GL_TEXTURE_2D, Textures.grassSide)
				case 2 => glBindTexture(GL_TEXTURE_2D, Textures.dirt)
			}
			glBegin(GL_QUADS)
			// glColor4f(1.0f,1.0f,0.0f, alpha)
			glTexCoord2f(1f, 1f)
			glVertex3f( 1.0f,-1.0f,-1.0f)
			glTexCoord2f(0f, 1f)
			glVertex3f(-1.0f,-1.0f,-1.0f)
			glTexCoord2f(0f, 0f)
			glVertex3f(-1.0f, 1.0f,-1.0f)
			glTexCoord2f(1f, 0f)
			glVertex3f( 1.0f, 1.0f,-1.0f)
			glEnd()
		}
		if (voxel.left) {
			voxel.blockType match {
				case 1 => glBindTexture(GL_TEXTURE_2D, Textures.grassSide)
				case 2 => glBindTexture(GL_TEXTURE_2D, Textures.dirt)
			}
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
		if (voxel.right) {
			voxel.blockType match {
				case 1 => glBindTexture(GL_TEXTURE_2D, Textures.grassSide)
				case 2 => glBindTexture(GL_TEXTURE_2D, Textures.dirt)
			}
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
