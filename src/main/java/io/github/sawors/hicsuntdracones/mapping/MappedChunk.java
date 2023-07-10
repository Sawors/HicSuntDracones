package io.github.sawors.hicsuntdracones.mapping;

import org.bukkit.World;

public class MappedChunk extends WorldChunk{
    WorldTile[] tiles;
    public MappedChunk(World world, int x, int z, WorldTile[] tiles) {
        super(world, x, z);
        this.tiles = tiles;
    }
    
    public WorldTile[] getTiles() {
        return tiles;
    }
}
