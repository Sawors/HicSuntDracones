package io.github.sawors.hicsuntdracones.mapping;

import org.bukkit.block.Biome;

public record WorldTile(int x, int z, int maxY, Biome biome, String type) {

}
