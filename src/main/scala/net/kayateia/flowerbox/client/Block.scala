/*
	Flowerbox
	Copyright 2018 Kayateia

	Permission is hereby granted, free of charge, to any person obtaining a copy of this software
	and associated documentation files (the "Software"), to deal in the Software without
	restriction, including without limitation the rights to use, copy, modify, merge, publish,
	distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
	Software is furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in all copies or
	substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
	BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
	NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
	DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.kayateia.flowerbox.client

// This isn't actually used yet.
trait Flavor {
	val top: TextureCoord = null
	val bottom: TextureCoord = null
	val left: TextureCoord = null
	val right: TextureCoord = null
	val front: TextureCoord = null
	val back: TextureCoord = null
}

case class Air() extends Flavor {
}

case class Dirt() extends Flavor {
	override val top = Textures.atlas.coordsOf(Textures.dirt)
	override val bottom = top
	override val left = top
	override val right = top
	override val front = top
	override val back = top
}

case class Grass() extends Flavor {
	override val top = Textures.atlas.coordsOf(Textures.grassTop)
	override val bottom = Textures.atlas.coordsOf(Textures.dirt)
	override val left = Textures.atlas.coordsOf(Textures.grassSide)
	override val right = left
	override val front = left
	override val back = left
}

case class Snow() extends Flavor {
	override val top = Textures.atlas.coordsOf(Textures.snowTop)
	override val bottom = Textures.atlas.coordsOf(Textures.dirt)
	override val left = Textures.atlas.coordsOf(Textures.grassSnowedSide)
	override val right = left
	override val front = left
	override val back = left
}

class Block(val flavor: Flavor)
