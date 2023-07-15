package io.github.sawors.hicsuntdracones.mapping;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.sawors.hicsuntdracones.Main;
import io.github.sawors.hicsuntdracones.SLogger;
import io.github.sawors.hicsuntdracones.WorldMapManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Map;

public class WorldRenderer {
    
    
    
    private final World world;
    private final File tileSaveFile;
    private final SLogger logger;
    
    public WorldRenderer(World world){
        this.world = world;
        this.tileSaveFile = WorldMapManager.getInstance(world).getTileSaveFile();
        this.logger = Main.logger();
    }
    
    public void renderMap(RenderType renderType) {
        
        // read the save data
        JsonObject saveJson = new JsonObject();
        try(Reader r = new FileReader(tileSaveFile)){
            JsonObject oldData = new Gson().fromJson(r,JsonObject.class);
            if(oldData != null){
                for(Map.Entry<String, JsonElement> entry : oldData.entrySet()){
                    saveJson.add(entry.getKey(),entry.getValue());
                }
            }
        } catch (
                IOException e){
            e.printStackTrace();
        }
        // building the tile array
        WorldTile[] tiles = new WorldTile[saveJson.size()];
        int height = 0;
        int width = 0;
        final String coordinateSeparator = ",";
        try{
            int[] xCoords = saveJson.keySet().stream().mapToInt(c -> Integer.parseInt(c.substring(0,c.indexOf(coordinateSeparator)))).toArray();
            int[] zCoords = saveJson.keySet().stream().mapToInt(c -> Integer.parseInt(c.substring(c.indexOf(coordinateSeparator)+1))).toArray();
            int maxX = Arrays.stream(xCoords).max().orElse(0);
            int minX = Arrays.stream(xCoords).min().orElse(0);
            int maxZ = Arrays.stream(zCoords).max().orElse(0);
            int minZ = Arrays.stream(zCoords).min().orElse(0);
            height = maxZ-minZ;
            width = maxX-minX;
        } catch (IndexOutOfBoundsException | NumberFormatException e){
            logger.logAdmin("error while parsing save data for world "+world.getName());
        }
        int index = 0;
        for(Map.Entry<String,JsonElement> entry : saveJson.entrySet()){
            String key = entry.getKey();
            JsonObject data = entry.getValue().getAsJsonObject();
            int x = Integer.parseInt(entry.getKey().substring(0,key.indexOf(coordinateSeparator)));
            int z = Integer.parseInt(entry.getKey().substring(key.indexOf(coordinateSeparator)+1));
            // TODO : add a way to avoid extracting the data if it's not needed for the rendering
            int maxY = Integer.parseInt(data.get(WorldTile.FIELD_MAX_Y).toString().replace("\"","").replace("'",""));
            NamespacedKey biomeKey = NamespacedKey.fromString(data.get(WorldTile.FIELD_BIOME).toString().replace("\"","").replace("'",""));
            String type = data.get(WorldTile.FIELD_TYPE).toString().replace("\"","").replace("'","");
            Material material = Material.AIR;
            try{
                material = Material.valueOf(data.get(WorldTile.FIELD_BLOCK).toString().replace("\"","").replace("'",""));
            } catch (IllegalArgumentException | NullPointerException ignored){}
            
            tiles[index] = new WorldTile(x,z,maxY,biomeKey,type,material);
            
            index++;
        }
        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        for(WorldTile tile : tiles){
        
        }
    }
    
    public enum RenderType {
        BIOME,
        HEIGHT,
        BLOCK,
        FANCY_OLD_NO_COLOR,
        FANCY_OLD_COLOR
    }
}
