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
