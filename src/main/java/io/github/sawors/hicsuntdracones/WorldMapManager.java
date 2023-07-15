package io.github.sawors.hicsuntdracones;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.sawors.hicsuntdracones.mapping.MappedChunk;
import io.github.sawors.hicsuntdracones.mapping.WorldRegion;
import io.github.sawors.hicsuntdracones.mapping.WorldTile;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class WorldMapManager {
    
    private final static Map<World, WorldMapManager> worldManagers = new HashMap<>();
    // file storage
    private final static File regionRootDirectory = new File(Main.getPlugin().getDataFolder().getPath() +File.separator+ "regions");
    // world and world directory
    private final World world;
    private final File worldDirectory;
    private final File tileSaveFile;
    // save thread
    ExecutorService saveThread = Executors.newSingleThreadExecutor();
    
    protected WorldMapManager(World world) {
        this.world = world;
        this.worldDirectory = new File(regionRootDirectory.getPath()+File.separator+world.getName());
        this.tileSaveFile = new File(worldDirectory.getPath()+File.separator+"tiles.json");
        
        worldDirectory.mkdirs();
        try{
            tileSaveFile.createNewFile();
            // put the world manager only if the save file can be created / reached
            worldManagers.put(world,this);
        } catch (IOException e){
            Bukkit.getLogger().log(Level.WARNING, "Failed to create the tile data file for world "+world.getName()+" !");
        }
    }
    
    public static WorldMapManager getInstance(World world){
        return worldManagers.getOrDefault(world,new WorldMapManager(world));
    }
    
    /**
     *
     * @param data a set containing all the chunks to be saved. Please note that if the chunks are not yet mapped
     *             or if a chunk has not yet been generated by the server its data will not be added to the save file.
     */
    public void saveData(Set<MappedChunk> data){
        saveThread.execute(() -> {
            JsonObject saveJson = new JsonObject();
            try(Reader r = new FileReader(tileSaveFile)){
                JsonObject oldData = new Gson().fromJson(r,JsonObject.class);
                if(oldData != null){
                    for(Map.Entry<String, JsonElement> entry : oldData.entrySet()){
                        saveJson.add(entry.getKey(),entry.getValue());
                    }
                }
            } catch (IOException e){
                e.printStackTrace();
            }
            for(MappedChunk chunk : data){
                if(chunk != null){
                    for(WorldTile tile : chunk.getTiles()){
                        if(tile != null){
                            String section = tile.x()+","+tile.z();
                            saveJson.add(section,tile.getSaveData());
                        }
                    }
                }
            }
            
            try(Writer out = new FileWriter(tileSaveFile)){
                Gson gson = new GsonBuilder().create();
                out.write(gson.toJson(saveJson));
            } catch (IOException e){
                e.printStackTrace();
            }
        });
    }
    
    public File getTileSaveFile() {
        return tileSaveFile;
    }
    
    
//    public void saveData(WorldRegion region){
//        YamlConfiguration saveConfig = new YamlConfiguration();
//
//        new File(regionRootDirectory.getPath()+File.separator+region.world().getName()).mkdirs();
//
//        File saveFile = new File(regionRootDirectory.getPath()+File.separator+region.world().getName()+File.separator+region.getFileName().substring(0,region.getFileName().lastIndexOf("."))+".yml");
//
//        int seaLevel = region.world().getSeaLevel();
//
//        // the resolution of the map goes BY DESIGN down to tiles of 8x8 blocks.
//        // so for a single chunk there are 4 tiles being registered.
//        //
//        // The structure of a save file can be seen in the resource directory of this jar in the file region_template.yml
//        ConfigurationSection rootSection = saveConfig.createSection(TILES_FIELD);
//        // tiles coordinates go from 0-0 to 63-63
//        List<ChunkSnapshot> sortedChunks = Lists.newArrayList(region.chunks());
//        sortedChunks.removeIf(Objects::isNull);
//        sortedChunks.sort((c1,c2) -> {
//            if(c1.getX() == c2.getX()){
//                return c1.getZ()-c2.getZ();
//            } else {
//                return c1.getX()-c2.getX();
//            }
//        });
//
//        // Simple tile rendre to preview the result, DEV ONLY
//        BufferedImage earlyRender = new BufferedImage(64,64,BufferedImage.TYPE_INT_RGB);
//
//        BiomeProvider biomeProvider = region.world().getBiomeProvider();
//        if(biomeProvider == null) biomeProvider = region.world().vanillaBiomeProvider();
//        Map<Biome,Integer> colorMap = new HashMap<>();
//
//        Main.logAdmin("BiomeProvider");
//        List<Biome> biomeList = List.copyOf(biomeProvider.getBiomes(region.world()));
//        int max = biomeList.stream().max(Comparator.comparingInt(Biome::ordinal)).orElse(Biome.PLAINS).ordinal();
//        int min = biomeList.stream().min(Comparator.comparingInt(Biome::ordinal)).orElse(Biome.PLAINS).ordinal();
//
//        Main.logAdmin(max);
//        Main.logAdmin(min);
//
//        for(Biome b : biomeList){
//            colorMap.put(b, Color.HSBtoRGB(
//                    (float) (b.ordinal()-min) / (max-min),
//                    .8f,
//                    1
//            ));
//        }
//
//        Main.logAdmin(colorMap);
//
//        for(ChunkSnapshot chunk : sortedChunks){
//            if(chunk != null){
//                // chunk relative coordinates go from 0-0 to 31-31
//                int relX = chunk.getX()-(region.x()*32);
//                int relZ = chunk.getZ()-(region.z()*32);
//
//                // splitting the chunk into sections
//                for(int x = 0; x < 2; x++){
//                    for(int z = 0; z < 2; z++){
//                        ConfigurationSection tileSection = rootSection.createSection(((relX*2)+x)+"-"+((relZ*2)+z));
//                        WorldTile tile = new WorldTile(chunk.getX(),chunk.getZ(),chunk.getHighestBlockYAt((x*8),(z*8)),chunk.getBiome((x*8),seaLevel+16,(z*8)));
//                        tileSection.set(BIOME_FIELD,tile.biome().getKey().toString());
//                        tileSection.set(HEIGHT_FIELD,tile.maxY());
//
//                        earlyRender.setRGB(((relX*2)+x),((relZ*2)+z),colorMap.getOrDefault(tile.biome(),0x000000));
//                    }
//                }
//            }
//        }
//
//        try {
//            saveConfig.save(saveFile);
//            ImageIO.write(earlyRender,"png",new File(saveFile.getPath()+".png"));
//            YamlConfiguration colors = new YamlConfiguration();
//            for(Map.Entry<Biome,Integer> entry : colorMap.entrySet()){
//                colors.set(entry.getKey().getKey().toString(),"#"+Integer.toHexString(entry.getValue()));
//            }
//            colors.save(new File(saveFile.getPath()+".colors.yml"));
//        } catch (IOException e){
//            Bukkit.getLogger().log(Level.WARNING,"Save file for region "+region.x()+", "+region.z()+" could not be created!");
//        }
//
//    }
    
    public void generateTiles(WorldRegion region){
    
    }
}