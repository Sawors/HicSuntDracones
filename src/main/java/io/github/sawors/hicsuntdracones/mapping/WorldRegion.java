package io.github.sawors.hicsuntdracones.mapping;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public record WorldRegion(int chunkX, int chunkZ, World world) {
    // the size (of the edge, in blocks) of the square regions used when subdividing the mapping area into regions
    final public static int regionChunkSize = 64;
    
    public static WorldRegion getRegionForCoordinates(Location location){
        Chunk chunk = location.getChunk();
        return new WorldRegion(chunk.getX(),chunk.getZ(),location.getWorld());
    }
    
    /**
     * Used to get the origin of this region in blocks in the common location system (x, y, z in blocks instead of chunks)
     * @return the {@link Location} of this region's origin
     */
    public Location getOrigin(){
        return new Location(world,chunkX*16,0,chunkZ*16);
    }
    
    /**
     *
     * @param origin the location of the bottom left corner of the selection
     * @param length the length on the X axis of the selection in blocks
     * @param height the height on the Z axis of the selection in blocks
     * @return an array containing all the {@link WorldRegion} necessary to cover the selection
     */
    public static WorldRegion[] split(Location origin, int length, int height){
        int xRegions = (int) Math.ceil(length/(regionChunkSize*16.0));
        int zRegions = (int) Math.ceil(height/(regionChunkSize*16.0));
        World world = origin.getWorld();
        
        int startChunkX = (int) Math.floor(origin.x()/16);
        int startChunkZ = (int) Math.floor(origin.z()/16);
        
        WorldRegion[] regions = new WorldRegion[xRegions*zRegions];
        int index = 0;
        for(int x = 0; x<xRegions; x++){
            for(int z = 0; z<zRegions; z++){
                regions[index] = new WorldRegion(startChunkX+(regionChunkSize*x),startChunkZ+(regionChunkSize*z),world);
                index++;
            }
        }
        return regions;
    }
}
