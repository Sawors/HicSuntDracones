package io.github.sawors.hicsuntdracones.mapping;

import io.github.sawors.hicsuntdracones.Main;
import io.github.sawors.hicsuntdracones.SLogger;
import io.github.sawors.hicsuntdracones.WorldMapManager;
import org.bukkit.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


public class WorldMapper {
    
    static SLogger logger = Main.logger();
    
    // only one world mapper is allowed per world
    private final static Map<String,WorldMapper> mappers = new HashMap<>(Bukkit.getWorlds().size());
    
    
    // TODO : add this to the config ↴
    private static final int workerThreadAmount = Runtime.getRuntime().availableProcessors()-2;
    private static final ExecutorService workerThreads = Executors.newFixedThreadPool(workerThreadAmount);
    //                                   TODO : add this to the config ↴
    private static final int tileSize = WorldTile.closestTileSize(8);
    
    private final int chunksPerBatch = 64*64;
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
            logger.logAdmin("region "+region.chunkX()+", "+region.chunkZ());
            regionTasks.add(() -> mapRegion(region, chunks -> {
                
                // save the region to a file
                logger.logAdmin("saving data for region "+region.chunkX()+", "+region.chunkZ());
                WorldMapManager.getInstance(world).saveData(chunks);
                
                if(!regionTasks.isEmpty()){
                    // run the next region in the queue
                    regionTasks.pop().run();
                } else {
                    // all regions generated !
                    logger.logAdmin("all regions generated !");
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
                    }
                    workerThreads.submit(() -> {
                        int emptyTileArraySize = (16/tileSize)*(16/tileSize);
                        WorldTile[] tiles = snapshot != null ? mapChunk(snapshot) : new WorldTile[emptyTileArraySize];
                        
                        mappedChunks.add(new MappedChunk(world, chunkX, chunkZ, tiles));
                        // check to see if the mapping is complete :
                        if(mappedChunks.size() == chunkAmount){
                            // mapping complete, the set is full !
                            //
                            // > REGION MAPPED <
                            //
                            logger.logAdmin("Mapping of region "+region.chunkX()+", "+region.chunkZ()+" finished !", true);
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
        int tilePerChunkSide = 16/tileSize;
        WorldTile[] tiles = new WorldTile[tilePerChunkSide*tilePerChunkSide];
        int centerOffset = tileSize/2;
        
        if(chunk != null){
            int i = 0;
            for(int x = 0; x<tilePerChunkSide; x++){
                for(int z = 0; z<tilePerChunkSide; z++){
                    int tileX = x*tileSize;
                    int tileZ = z*tileSize;
                    int tileMaxY = chunk.getHighestBlockYAt(tileX+centerOffset,tileZ+centerOffset);
                    Material material = chunk.getBlockType(tileX+centerOffset-1,tileMaxY,tileZ+centerOffset-1);
                    tiles[i] = new WorldTile(
                            (chunk.getX()*tilePerChunkSide)+x,
                            (chunk.getZ()*tilePerChunkSide)+z,
                            tileMaxY,
                            chunk.getBiome(tileX,tileMaxY,tileZ).getKey(),
                            WorldTile.TileType.DEFAULT,
                            material
                    );
                    i++;
                }
            }
        }
        
        return tiles;
    }
    
    protected ExecutorService getWorkerThreads() {
        return workerThreads;
    }
    
    private WorldChunk[][] splitRegion(int minX, int maxX, int minZ, int maxZ){
        int startChunkX = Math.floorDiv(minX,16);
        int startChunkZ = Math.floorDiv(minZ,16);
        int chunkAmountX = (int) Math.ceil((maxX-(startChunkX*16))/16.0);
        int chunkAmountZ = (int) Math.ceil((maxZ-(startChunkZ*16))/16.0);
        
        int packAmount = (int) Math.ceil(((double) chunkAmountX*chunkAmountZ)/chunksPerBatch);
        
        WorldChunk[][] split = new WorldChunk[packAmount][chunksPerBatch];
        int chunkIndex = 0;
        for(int x = 0; x<chunkAmountX; x++){
            for(int z = 0; z<chunkAmountZ; z++){
                int batch = Math.floorDiv(chunkIndex,chunksPerBatch);
                int internalBatchIndex = chunkIndex%chunksPerBatch;
                split[batch][internalBatchIndex] = new WorldChunk(world,startChunkX+x,startChunkZ+z);
                chunkIndex++;
            }
        }
        
        return split;
        
        //        int xRegions = (int) Math.ceil(length/(regionChunkSize*16.0));
        //        int zRegions = (int) Math.ceil(height/(regionChunkSize*16.0));
        //        World world = origin.getWorld();
        //
        //        int startChunkX = (int) Math.floor(origin.x()/16);
        //        int startChunkZ = (int) Math.floor(origin.z()/16);
        //
        //        WorldRegion[] regions = new WorldRegion[xRegions*zRegions];
        //        int index = 0;
        //        for(int x = 0; x<xRegions; x++){
        //            for(int z = 0; z<zRegions; z++){
        //                regions[index] = new WorldRegion(startChunkX+(regionChunkSize*x),startChunkZ+(regionChunkSize*z),world);
        //                index++;
        //            }
        //        }
        //        return regions;
    }
}
