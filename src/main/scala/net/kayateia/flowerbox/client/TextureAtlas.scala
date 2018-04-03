package net.kayateia.flowerbox.client

import java.io.File
import java.nio.{ByteBuffer, ByteOrder}

import org.lwjgl.opengl.GL11._
import slim.texture.io.PNGDecoder

import scala.collection.mutable.ArrayBuffer

case class TextureCoord(s1: Float, t1: Float, s2: Float, t2: Float)
class TextureAtlas(val eachTxrSize: Int) {
	private val queuedImages = new ArrayBuffer[String]
	private var glTxrId = 0
	private var bigTxrSize = 0
	private var coords: Array[TextureCoord] = Array()

	def load(filenames: Seq[String]) {
		queuedImages ++= filenames
	}

	lazy val glTextureId: Int = genTextureId

	def coordsOf(index: Int): TextureCoord = coords(index)

	private def genTextureId: Int = {
		bigTxrSize = txrSize
		val txrPerRow: Int = bigTxrSize / eachTxrSize
		val buffer = ByteBuffer.allocateDirect(bigTxrSize * bigTxrSize * 4)
		buffer.order(ByteOrder.nativeOrder)

		val poses =
			(for (y <- 0 until txrPerRow;
					x <- 0 until txrPerRow)
				yield (x * eachTxrSize * 4) + (y * eachTxrSize * bigTxrSize * 4)).iterator
		coords =
			(for (y <- 0 until txrPerRow;
					x <- 0 until txrPerRow)
				yield TextureCoord(
					(1f * x * eachTxrSize) / bigTxrSize,
					(1f * y * eachTxrSize) / bigTxrSize,
					((1f * x * eachTxrSize) + (eachTxrSize-1)) / bigTxrSize,
					((1f * y * eachTxrSize) + (eachTxrSize-1)) / bigTxrSize
				)).toArray

		val stride = bigTxrSize * 4
		for (fn <- queuedImages) {
			val offset = poses.next()
			println(fn, " at ", offset)
			loadPng(fn, buffer, offset, stride)
		}

		buffer.flip()


		glTxrId = glGenTextures()
		glBindTexture(GL_TEXTURE_2D, glTxrId)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, bigTxrSize, bigTxrSize, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)

		glTxrId
	}

	private def txrSize: Int = {
		val minTxrMultiple = math.ceil(math.sqrt(queuedImages.length))
		val minTxrSize = minTxrMultiple * eachTxrSize

		val p2s = (0 to 12).iterator.map(math.pow(2, _)).dropWhile(_ < minTxrSize).take(1)
		if (!p2s.hasNext)
			throw new Exception("fooz")

		p2s.next().asInstanceOf[Int]
	}

	private def loadPng(path: String, buffer: ByteBuffer, offset: Int, stride: Int) {
		println("Loading " + path)
		val decoder = new PNGDecoder(new File(path).toURI.toURL.openStream())
		val width = decoder.getWidth
		val height = decoder.getHeight

		buffer.position(offset)
		decoder.decode(buffer, stride, PNGDecoder.Format.RGBA)
	}
}
