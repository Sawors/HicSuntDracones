package io.github.sawors.hicsuntdracones;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.sawors.hicsuntdracones.mapping.WorldMapper;
import io.github.sawors.hicsuntdracones.mapping.WorldRenderer;
import io.github.sawors.hicsuntdracones.mapping.WorldTile;
import io.github.sawors.hicsuntdracones.mapping.painters.WaterMapTilePainter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class MapCommand implements TabExecutor {
    
    SLogger logger = Main.logger();
    
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        
        if(strings.length >= 1){
            World world = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment().equals(World.Environment.NORMAL)).findFirst().orElse(Bukkit.getWorlds().get(0));
            
            switch(strings[0]){
                case "radius" -> {
                    int radius = strings.length >= 2 ? Integer.parseInt(strings[1]) : 512;
                    WorldMapper mapper = WorldMapper.getMapper(world);
                    logger.logAdmin("mapping world ["+world.getName()+"] with a radius of "+radius);
                    mapper.mapRadius(0,0,radius);
                }
                case "render" -> {
                    WorldRenderer renderer = new WorldRenderer(world);
                    renderer.renderMap(new WaterMapTilePainter(world));
                }
                case "chunks" -> {
                    logger.logAdmin("there is "+world.getLoadedChunks().length+" loaded chunks in "+world.getName());
                }
                case "test" -> {
                    Gson gson = new Gson();
                    JsonObject json = gson.fromJson("{\"0,0\":{\"biome\":\"minecraft:forest\",\"type\":\"basic\",\"maxY\":100,\"block\":\"minecraft:light_blue_wool\"}}", JsonObject.class);
                    for(Map.Entry<String,JsonElement> entry : json.entrySet()){
                        logger.logAdmin(entry.getKey());
                        logger.logAdmin(entry.getValue());
                        logger.logAdmin(entry.getValue().getAsJsonObject().get(WorldTile.FIELD_BIOME).toString().replace("\"","").replace("'",""));
                    }
                }
                case "purge" ->  {
                    WorldMapManager.getInstance(world).getTileSaveFile().delete();
                }
            }
            return true;
        }
        
        return false;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
