package io.github.sawors.hicsuntdracones.mapping;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import org.bukkit.block.Biome;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

// the X and Z values are according to the tile grid, not the block or the chunk one
public record WorldTile(int x, int z, int maxY, Biome biome, String type) {
    public static ImmutableList<Integer> tileSizes = ImmutableList.of(
            1,2,4,8,16
    );
    
    public static int closestTileSize(int value){
        return tileSizes.stream().min(Comparator.comparingInt(a -> a-value)).orElse(8);
    }
    
    public JsonObject getSaveData(){
        JsonObject tileData = new JsonObject();
        tileData.addProperty("biome",biome().getKey().toString());
        tileData.addProperty("type",type());
        tileData.addProperty("maxY",maxY);
        return tileData;
    }
}
