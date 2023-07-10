package io.github.sawors.hicsuntdracones.mapping;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class WorldMapper {
    
    // TODO : add this to the config ↴
    private static int workerThreadAmount = Runtime.getRuntime().availableProcessors()-2;
    //
    private static final ExecutorService workerThreads = Executors.newFixedThreadPool(workerThreadAmount);
    
    
    private final World world;
    
    public WorldMapper(World world){
        this.world = world;
    }
    
    public void mapRadius(int radius) {
        mapRadius(0, 0, radius, (chunks) -> {});
    }
    
    public void mapRadius(int centerX, int centerZ, int radius, Consumer<Set<MappedChunk>> callback) {
        
        // making them final just to assert their thread-safe(ish) nature
        final int chunkRadius = radius/16;
        final int chunkAmount = chunkRadius*chunkRadius*4;
        final Set<MappedChunk> mappedChunks = ConcurrentHashMap.newKeySet(chunkAmount);
        
        for(int x = -chunkRadius+centerX; x <= chunkRadius+centerX; x++){
            for(int z = -chunkRadius+centerZ; z <= chunkRadius+centerZ; z++){
                final int chunkX = x;
                final int chunkZ = z;
                workerThreads.submit(() -> {
                    world.getChunkAtAsync(chunkX, chunkZ, (Consumer<Chunk>) chunk -> {
                        // WARNING :
                        // it is absolutely necessary that includeMaxblocky and includeBiome are set to true
                        // in chunk.getChunkSnapshot(includeMaxblocky, includeBiome, includeBiomeTempRain) !!!
                        ChunkSnapshot snapshot = chunk.getChunkSnapshot(true,true,false);
                        workerThreads.submit(() -> {
                            WorldTile[] tiles = mapChunk(snapshot);
                            mappedChunks.add(new MappedChunk(world, chunkX, chunkZ, tiles));
                            
                            // check to see if the mapping is complete :
                            if(mappedChunks.size() == chunkAmount){
                                // mapping complete, the set is full !
                                callback.accept(mappedChunks);
                            }
                        });
                        
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
    public class TileType {
        final public static String DEFAULT = "basic";
        final public static String VILLAGE = "village";
        final public static String OUTPOST = "pillager_outpost";
    }
}
