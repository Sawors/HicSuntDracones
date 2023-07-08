package io.github.sawors.hicsuntdracones.mapping;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;

public class WorldMapper {
    
    private final World world;
    
    public WorldMapper(World world){
        this.world = world;
    }
    
    public String map(int x, int z){
        
        Chunk chunk = world.getChunkAt(x,z,false);
        Biome b = chunk.getWorld().getHighestBlockAt((x*16)+8,(z*16)+8).getBiome();
        return b.getKey().getKey();
    }
    
    public World getWorld(){
        return world;
    }
}
