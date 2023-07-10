package io.github.sawors.hicsuntdracones;

import com.google.common.collect.Lists;
import io.github.sawors.hicsuntdracones.mapping.WorldRegion;
import io.github.sawors.hicsuntdracones.mapping.WorldTile;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.BiomeProvider;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.logging.Level;

public class MapRegionManager {
    
    private final static Map<World,MapRegionManager> worldManagers = new HashMap<>();
    // file storage
    private final static File regionRootDirectory = new File(Main.getPlugin().getDataFolder().getPath() +File.separator+ "regions");
    // tile storage fields
    public static final String BIOME_FIELD = "biome";
    public static final String HEIGHT_FIELD = "y";
    public static final String TILES_FIELD = "tiles";
    public static final String TYPE_FIELD = "type";
    // world and world directory
    private final World world;
    private final File worldDirectory;
    private final File tileDirectory;
    
    protected MapRegionManager(World world) {
        this.world = world;
        this.worldDirectory = new File(regionRootDirectory.getPath()+File.separator+world.getName());
        this.tileDirectory = new File(worldDirectory.getPath()+File.separator+"tiles");
        
        regionRootDirectory.mkdirs();
        tileDirectory.mkdirs();
    }
    
    public static MapRegionManager getInstance(World world){
        return worldManagers.getOrDefault(world,new MapRegionManager(world));
    }
    
    /**
     *
     * @param region The {@link WorldRegion} for which the data must be created and written to disk.
     *               This method does <b>NOT</b> render the tiles. Its only purpose is to save the data map
     *               used to later do the rendering process.
     */
    public void saveData(WorldRegion region){
        YamlConfiguration saveConfig = new YamlConfiguration();
        
        new File(regionRootDirectory.getPath()+File.separator+region.world().getName()).mkdirs();
        
        File saveFile = new File(regionRootDirectory.getPath()+File.separator+region.world().getName()+File.separator+region.getFileName().substring(0,region.getFileName().lastIndexOf("."))+".yml");
        
        int seaLevel = region.world().getSeaLevel();
        
        // the resolution of the map goes BY DESIGN down to tiles of 8x8 blocks.
        // so for a single chunk there are 4 tiles being registered.
        //
        // The structure of a save file can be seen in the resource directory of this jar in the file region_template.yml
        ConfigurationSection rootSection = saveConfig.createSection(TILES_FIELD);
        // tiles coordinates go from 0-0 to 63-63
        List<ChunkSnapshot> sortedChunks = Lists.newArrayList(region.chunks());
        sortedChunks.removeIf(Objects::isNull);
        sortedChunks.sort((c1,c2) -> {
            if(c1.getX() == c2.getX()){
                return c1.getZ()-c2.getZ();
            } else {
                return c1.getX()-c2.getX();
            }
        });
        
        // Simple tile rendre to preview the result, DEV ONLY
        BufferedImage earlyRender = new BufferedImage(64,64,BufferedImage.TYPE_INT_RGB);
        
        BiomeProvider biomeProvider = region.world().getBiomeProvider();
        if(biomeProvider == null) biomeProvider = region.world().vanillaBiomeProvider();
        Map<Biome,Integer> colorMap = new HashMap<>();
        
        Main.logAdmin("BiomeProvider");
        List<Biome> biomeList = List.copyOf(biomeProvider.getBiomes(region.world()));
        int max = biomeList.stream().max(Comparator.comparingInt(Biome::ordinal)).orElse(Biome.PLAINS).ordinal();
        int min = biomeList.stream().min(Comparator.comparingInt(Biome::ordinal)).orElse(Biome.PLAINS).ordinal();
        
        Main.logAdmin(max);
        Main.logAdmin(min);
        
        for(Biome b : biomeList){
            colorMap.put(b, Color.HSBtoRGB(
                    (float) (b.ordinal()-min) / (max-min),
                    .8f,
                    1
            ));
        }
        
        Main.logAdmin(colorMap);
        
        for(ChunkSnapshot chunk : sortedChunks){
            if(chunk != null){
                // chunk relative coordinates go from 0-0 to 31-31
                int relX = chunk.getX()-(region.x()*32);
                int relZ = chunk.getZ()-(region.z()*32);
                
                // splitting the chunk into sections
                for(int x = 0; x < 2; x++){
                    for(int z = 0; z < 2; z++){
                        ConfigurationSection tileSection = rootSection.createSection(((relX*2)+x)+"-"+((relZ*2)+z));
                        // TODO : find the most relevant sample point : origin of the tile or its center ?
                        WorldTile tile = new WorldTile(chunk.getX(),chunk.getZ(),chunk.getHighestBlockYAt((x*8),(z*8)),chunk.getBiome((x*8),seaLevel+16,(z*8)));
                        tileSection.set(BIOME_FIELD,tile.biome().getKey().toString());
                        tileSection.set(HEIGHT_FIELD,tile.maxY());
                        
                        earlyRender.setRGB(((relX*2)+x),((relZ*2)+z),colorMap.getOrDefault(tile.biome(),0x000000));
                    }
                }
            }
        }
        
        try {
            saveConfig.save(saveFile);
            ImageIO.write(earlyRender,"png",new File(saveFile.getPath()+".png"));
            YamlConfiguration colors = new YamlConfiguration();
            for(Map.Entry<Biome,Integer> entry : colorMap.entrySet()){
                colors.set(entry.getKey().getKey().toString(),"#"+Integer.toHexString(entry.getValue()));
            }
            colors.save(new File(saveFile.getPath()+".colors.yml"));
        } catch (IOException e){
            Bukkit.getLogger().log(Level.WARNING,"Save file for region "+region.x()+", "+region.z()+" could not be created!");
        }
        
    }
    
    public void generateTiles(WorldRegion region){
    
    }
}
