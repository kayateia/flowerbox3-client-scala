package net.kayateia.flowerbox.client

object Textures {
	val atlas = new TextureAtlas(32)

	private val pathRoot: String = "./textures/"
	atlas.load(Seq(
		"grass_side.png",
		"grass_top.png",
		"dirt.png",
		"grass_side_snowed.png",
		"snow.png").map(pathRoot + _))

	val grassSide = 0
	val grassTop = 1
	val dirt = 2
	val grassSnowedSide = 3
	val snowTop = 4
}
