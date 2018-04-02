package net.kayateia.flowerbox.client

import java.io.File
import java.net.URL
import java.nio.{ByteBuffer, ByteOrder}

import org.lwjgl.opengl.GL11._
import slim.texture.io.PNGDecoder

object Textures {
	lazy val grassSide: Int = loadOne("grass_side.png")
	lazy val grassTop: Int = loadOne("grass_top.png")
	lazy val dirt: Int = loadOne("dirt.png")

	def loadAll() {
		grassSide
		grassTop
		dirt
	}

	private val pathRoot: String = "./textures/"

	private def loadOne(path: String): Int = {
		println("Loading " + pathRoot + path)
		val png = loadPng(pathRoot + path)
		val txrId = genTexture(png._1, png._2, png._3)
		println(txrId)
		txrId
	}

	private def loadPng(path: String): (Int, Int, ByteBuffer) = {
		val decoder = new PNGDecoder(new File(path).toURI.toURL.openStream())
		val width = decoder.getWidth
		val height = decoder.getHeight

		val buffer = ByteBuffer.allocateDirect(width * height * 4)
		buffer.order(ByteOrder.nativeOrder)
		decoder.decode(buffer, width * 4, PNGDecoder.Format.RGBA)
		buffer.flip()

		(width, height, buffer)
		// ImageIO.read(new File(pathRoot + path))
	}

	//private def getPngBuffer(image: PngImage): ByteBuffer = {
		// val buffer = ByteBuffer.allocateDirect(
		/* val data = image.getImageData
		val buffer = ByteBuffer.allocateDirect(data.length)
		buffer.order(ByteOrder.nativeOrder)
		buffer.put(data, 0, data.length)
		buffer.flip()
		buffer */
		/* val result = new Array[Int](image.getWidth * image.getHeight * 3)
		val outSamples = new Array[Float](3)
		for (y <- 0 until image.getHeight; x <- 0 until image.getWidth) {
			val offset = 3 * y * image.getWidth + x
			image.getRaster.getPixel(x, y, outSamples)
			for (c <- 0 until 3)
				result(offset + c) = (outSamples(c) * 255).asInstanceOf[Int]
		}

		result */
	//}

	private def genTexture(width: Int, height: Int, image: ByteBuffer): Int = {
		// Create a new texture object in memory and bind it
		val texId = glGenTextures()
		glBindTexture(GL_TEXTURE_2D, texId)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
		// glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
		// glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image)

		texId
	}
}
