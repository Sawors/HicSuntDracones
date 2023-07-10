package io.github.sawors.hicsuntdracones.mapping;

import org.bukkit.World;

public class WorldChunk {
    final private World world;
    final private int x;
    final private int z;
    
    public WorldChunk(World world, int x, int z){
        this.world = world;
        this.x = x;
        this.z = z;
    }
    
    public World getWorld() {
        return world;
    }
    
    public int getX() {
        return x;
    }
    
    public int getZ() {
        return z;
    }
}
