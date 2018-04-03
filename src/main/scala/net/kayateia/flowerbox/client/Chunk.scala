package net.kayateia.flowerbox.client

import com.flowpowered.noise.module.source.Perlin
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL30._
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL13._
import org.lwjgl.opengl.GL20._

import scala.collection.mutable.ArrayBuffer

object Chunk {
	val xSize = 0.25f
	val zSize = 0.25f
}

class Chunk(val globalX: Float, val globalZ: Float) {
	def setup() {
		genLand()
	}

	lazy val displayList = genDL(genLand())
	lazy val buffers = genBuffers(displayList)

	// Convert from x,y,z to cubeMap index.
	private def coord(x: Int, y: Int, z: Int) = z*16*16 + y*16 + x

	// Generates the cube map.
	private def genLand(): Array[Int] = {
		val cubeMap = new Array[Int](16*16*16)

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

		cubeMap
	}

	// Represents one voxel derived from the cube map.
	case class Voxel(x: Int, y: Int, z: Int, blockType: Int,
					 top: Boolean = true, left: Boolean = true, right: Boolean = true, bottom: Boolean = true,
					 front: Boolean = true, back: Boolean = true) {
		def isEmpty = !top && !left && !right && !bottom && !front && !back

		// Voxel size
		def size = 0.5f

		// Each vertex will take 5 floats in the array.
		def toBuffer(dataArray: ArrayBuffer[Float], elementArray: ArrayBuffer[Int], textureArray: ArrayBuffer[Int]) {
			if (top) {
				val base = dataArray.length / 5
				elementArray ++= List (
					base+0, base+1, base+2, base+2, base+3, base+0
				)
				dataArray ++= List (
					x+size, y+size, z-size,
					1f, 0f,
					x-size, y+size, z-size,
					0f, 0f,
					x-size, y+size, z+size,
					0f, 1f,
					x+size, y+size, z+size,
					1f, 1f
				)
				textureArray += {
					blockType match {
						case 1 => Textures.grassTop
						case 2 => Textures.dirt
					}
				}
			}
			if (bottom) {
				val base = dataArray.length / 5
				elementArray ++= List (
					base+0, base+1, base+2, base+2, base+3, base+0
				)
				dataArray ++= List (
					x+size, y-size, z+size,
					1f, 0f,
					x-size, y-size, z+size,
					0f, 0f,
					x-size, y-size, z-size,
					0f, 1f,
					x+size, y-size, z-size,
					1f, 1f
				)
				textureArray += Textures.dirt
			}
			if (back) {
				val base = dataArray.length / 5
				elementArray ++= List (
					base+0, base+1, base+2, base+2, base+3, base+0
				)
				dataArray ++= List (
					x+size, y+size, z+size,
					1f, 0f,
					x-size, y+size, z+size,
					0f, 0f,
					x-size, y-size, z+size,
					0f, 1f,
					x+size, y-size, z+size,
					1f, 1f
				)
				textureArray += {
					blockType match {
						case 1 => Textures.grassSide
						case 2 => Textures.dirt
					}
				}
			}
			if (front) {
				val base = dataArray.length / 5
				elementArray ++= List (
					base+0, base+1, base+2, base+2, base+3, base+0
				)
				dataArray ++= List (
					x+size, y-size, z-size,
					1f, 1f,
					x-size, y-size, z-size,
					0f, 1f,
					x-size, y+size, z-size,
					0f, 0f,
					x+size, y+size, z-size,
					1f, 0f
				)
				textureArray += {
					blockType match {
						case 1 => Textures.grassSide
						case 2 => Textures.dirt
					}
				}
			}
			if (left) {
				val base = dataArray.length / 5
				elementArray ++= List (
					base+0, base+1, base+2, base+2, base+3, base+0
				)
				dataArray ++= List (
					x-size, y+size, z+size,
					1f, 0f,
					x-size, y+size, z-size,
					0f, 0f,
					x-size, y-size, z-size,
					0f, 1f,
					x-size, y-size, z+size,
					1f, 1f
				)
				textureArray += {
					blockType match {
						case 1 => Textures.grassSide
						case 2 => Textures.dirt
					}
				}
			}
			if (right) {
				val base = dataArray.length / 5
				elementArray ++= List (
					base+0, base+1, base+2, base+2, base+3, base+0
				)
				dataArray ++= List (
					x+size, y+size, z-size,
					1f, 0f,
					x+size, y+size, z+size,
					0f, 0f,
					x+size, y-size, z+size,
					0f, 1f,
					x-size, y-size, z-size,
					1f, 1f
				)
				textureArray += {
					blockType match {
						case 1 => Textures.grassSide
						case 2 => Textures.dirt
					}
				}
			}
		}
	}

	// Converts the cube map into a display list of voxels. Optimizes away as many as possible.
	private def genDL(cubeMap: Array[Int]): ArrayBuffer[Voxel] = {
		val displayList = ArrayBuffer[Voxel]()

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

		displayList
	}

	case class Buffers(vertsAndSt: ArrayBuffer[Float], elems: ArrayBuffer[Int], txrs: ArrayBuffer[Int])

	private def genBuffers(voxels: ArrayBuffer[Voxel]): Buffers = {
		val vas = new ArrayBuffer[Float]
		val es = new ArrayBuffer[Int]
		val ts = new ArrayBuffer[Int]
		for (v <- voxels) {
			v.toBuffer(vas, es, ts)
		}

		Buffers(vas, es, ts)
	}

	var vaoId: Int = 0
	var vboId: Int = 0
	var vboiId: Int = 0

	def createVertexArrays() {
		val vertByteBuffer = BufferUtils.createByteBuffer(buffers.vertsAndSt.length * 4);
		val vertFloatBuffer = vertByteBuffer.asFloatBuffer
		vertFloatBuffer.put(buffers.vertsAndSt.toArray, 0, buffers.vertsAndSt.length)
		vertFloatBuffer.flip()

		val indexByteBuffer = BufferUtils.createByteBuffer(buffers.elems.length * 2)
		val indexShortBuffer = indexByteBuffer.asShortBuffer
		for (e <- buffers.elems)
			indexShortBuffer.put(e.asInstanceOf[Short])
		indexShortBuffer.flip()

		vaoId = glGenVertexArrays()
		glBindVertexArray(vaoId)

		vboId = glGenBuffers()
		glBindBuffer(GL_ARRAY_BUFFER, vboId)
		glBufferData(GL_ARRAY_BUFFER, vertFloatBuffer, GL_STREAM_DRAW)

		// Put the position coordinates in attribute list 0
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 5*4, 0)
		// Put the color components in attribute list 1
		//glVertexAttribPointer(1, VertexData.colorElementCount, GL_FLOAT, false, VertexData.stride, VertexData.colorByteOffset)
		// Put the texture coordinates in attribute list 2
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 5*4, 3*4)

		glBindBuffer(GL_ARRAY_BUFFER, 0)
		glBindVertexArray(0)

		vboiId = glGenBuffers()
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboiId)
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexShortBuffer, GL_STATIC_DRAW)
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
	}

	def render(pId: Int) {
		glUseProgram(pId)

		// Bind the texture
		glActiveTexture(GL_TEXTURE0)
		glBindTexture(GL_TEXTURE_2D, Textures.dirt)

		// Bind to the VAO that has all the information about the vertices
		glBindVertexArray(vaoId)
		glEnableVertexAttribArray(0)
		glEnableVertexAttribArray(1)
		// GL20.glEnableVertexAttribArray(2);

		// Bind to the index VBO that has all the information about the order of the vertices
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboiId)

		// Draw the vertices
		glDrawElements(GL_TRIANGLES, buffers.elems.length, GL_UNSIGNED_SHORT, 0)

		// Put everything back to default (deselect)
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
		glDisableVertexAttribArray(0)
		glDisableVertexAttribArray(1)
		// GL20.glDisableVertexAttribArray(2);
		glBindVertexArray(0)

		glUseProgram(0)
	}
}
