package io.github.sawors.hicsuntdracones.mapping;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.sawors.hicsuntdracones.Main;
import io.github.sawors.hicsuntdracones.MapRegionManager;
import org.bukkit.*;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class WorldMapper {
    
    // only one world mapper is allowed per world
    private final static Map<String,WorldMapper> mappers = new HashMap<>(Bukkit.getWorlds().size());
    
    
    // TODO : add this to the config ↴
    private static final int workerThreadAmount = Runtime.getRuntime().availableProcessors()-2;
    private static final ExecutorService workerThreads = Executors.newFixedThreadPool(workerThreadAmount);
    //                                   TODO : add this to the config ↴
    private static final int tileSize = WorldTile.closestTileSize(8);
    
    private final World world;
    
    /**
     * Gets the {@link WorldMapper} associated with a specific world.
     * In case a mapper has already been created for the world,
     * this method will return the instance of the already existing mapper.
     * @param world the world the mapper should work with
     * @return the {@link WorldMapper} associated with this world
     */
    public static WorldMapper getMapper(World world){
        return mappers.getOrDefault(world.getName(),new WorldMapper(world));
    }
    
    private WorldMapper(World world){
        this.world = world;
        mappers.put(world.getName(),this);
    }
    
    /**
     *
     * @param centerX the X center of the square region to map, in blocks
     * @param centerZ the Z center of the square region to map, in blocks
     * @param radius the radius of the square region to map, in blocks
     */
    public void mapRadius(int centerX, int centerZ, int radius){
        LinkedList<Runnable> regionTasks = new LinkedList<>();
        
        WorldRegion[] regions = WorldRegion.split(new Location(world,Math.floorDiv(centerX-radius,16)*16,0,Math.floorDiv(centerZ-radius,16)*16),radius*2,radius*2);
        for(WorldRegion region : regions){
            Main.logAdmin("region "+region.chunkX()+", "+region.chunkZ());
            regionTasks.add(() -> mapRegion(region, chunks -> {
                if(!regionTasks.isEmpty()){
                    // save the region to a file
                    MapRegionManager.getInstance(world).saveData(chunks);
                    // run the next region in the queue
                    regionTasks.pop().run();
                } else {
                    // all regions generated !
                    Main.logAdmin("all regions generated !");
                }
            }));
        }
        if(!regionTasks.isEmpty()){
            regionTasks.pop().run();
        }
    }
    
    protected void mapRegion(WorldRegion region, Consumer<Set<MappedChunk>> callback) {
        int regionChunkSize = WorldRegion.regionChunkSize;
        
        int chunkStartX = region.chunkX();
        int chunkStartZ = region.chunkZ();
        
        // making them final just to assert their thread-safe(ish) nature
        final int chunkAmount = WorldRegion.regionChunkSize*WorldRegion.regionChunkSize;
        final Set<MappedChunk> mappedChunks = ConcurrentHashMap.newKeySet(chunkAmount);
        
        for(int x = chunkStartX; x < chunkStartX+regionChunkSize; x++){
            for(int z = chunkStartZ; z < chunkStartZ+regionChunkSize; z++){
                
                final int chunkX = x;
                final int chunkZ = z;
                
                world.getChunkAtAsync(chunkX, chunkZ, false, chunk -> {
                    // WARNING :
                    // it is absolutely necessary that includeMaxblocky and includeBiome are set to true
                    // in chunk.getChunkSnapshot(includeMaxblocky, includeBiome, includeBiomeTempRain) !!!
                    ChunkSnapshot snapshot = chunk != null ? chunk.getChunkSnapshot(true,true,false) : null;
                    if(chunk != null && chunk.isEntitiesLoaded()){
                        world.unloadChunk(chunk);
                        Main.logAdmin("unloading chunk "+chunk.getX()+", "+chunk.getZ(),true);
                    }
                    if(chunk == null){
                        Main.logAdmin("chunk "+chunkX+", "+chunkZ+" is null !",true);
                    }
                    workerThreads.submit(() -> {
                        WorldTile[] tiles = snapshot != null ? mapChunk(snapshot) : new WorldTile[4];
                        mappedChunks.add(new MappedChunk(world, chunkX, chunkZ, tiles));
                        if(snapshot != null){
                            Main.logAdmin("mapped chunk "+snapshot.getX()+", "+snapshot.getZ(),true);
                        }
                        // check to see if the mapping is complete :
                        if(mappedChunks.size() == chunkAmount){
                            // mapping complete, the set is full !
                            //
                            // > REGION MAPPED <
                            //
                            Main.logAdmin("Mapping of region "+region.chunkX()+", "+region.chunkZ()+" finished !", true);
                            callback.accept(mappedChunks);
                        }
                    });
                    
                });
            }
        }
        
    }
    
    public World getWorld(){
        return world;
    }
    
    private WorldTile[] mapChunk(ChunkSnapshot chunk){
        WorldTile[] tiles = new WorldTile[4];
        
        if(chunk != null){
            int i = 0;
            for(int x = 0; x<2; x++){
                for(int z = 0; z<2; z++){
                    int tileX = x*8;
                    int tileZ = z*8;
                    int tileMaxY = chunk.getHighestBlockYAt(tileX+4,tileZ+4);
                    tiles[i] = new WorldTile((chunk.getX()*2)+tileX,(chunk.getZ()*2)+tileZ,tileMaxY, chunk.getBiome(tileX,tileMaxY,tileZ), TileType.DEFAULT);
                    i++;
                }
            }
        }
        
        return tiles;
    }
    
    // just using this instead of an enum for convenience
    public static final class TileType {
        final public static String DEFAULT = "basic";
        final public static String VILLAGE = "village";
        final public static String OUTPOST = "pillager_outpost";
    }
}
