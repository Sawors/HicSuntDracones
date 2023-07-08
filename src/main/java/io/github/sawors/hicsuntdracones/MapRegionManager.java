package io.github.sawors.hicsuntdracones;

import io.github.sawors.hicsuntdracones.mapping.WorldRegion;

import java.io.File;
import java.io.IOException;

public class MapRegionManager {
    
    // file storage
    private final static File regionRootDirectory = new File(Main.getPlugin().getDataFolder().getPath() +File.separator+ "regions");
    private final static File tileDirectory = new File(regionRootDirectory.getPath() +File.separator+ "tiles");
    
    protected MapRegionManager() {
        try{
            regionRootDirectory.createNewFile();
            tileDirectory.createNewFile();
        } catch (IOException e){
            Main.logAdmin("Could not create the region directory !");
            e.printStackTrace();
        }
    }
    
    public static MapRegionManager getInstance(){
        return Main.regionManager;
    }
    
    public void saveData(WorldRegion region){
    
    }
    
    public void generateTiles(WorldRegion region){
    
    }
}
