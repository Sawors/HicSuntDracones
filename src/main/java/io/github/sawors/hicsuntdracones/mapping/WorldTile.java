package io.github.sawors.hicsuntdracones.mapping;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import java.util.Comparator;

// the X and Z values are according to the tile grid, not the block or the chunk one
public record WorldTile(int x, int z, int maxY, NamespacedKey biome, String type, Material material) {
    
    public static final String FIELD_BIOME = "biome";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_MAX_Y = "maxY";
    public static final String FIELD_BLOCK = "block";
    
    public static ImmutableList<Integer> tileSizes = ImmutableList.of(
            1,2,4,8,16
    );
    
    public static int closestTileSize(int value){
        return tileSizes.stream().min(Comparator.comparingInt(a -> Math.abs(a-value))).orElse(8);
    }
    
    public JsonObject getSaveData(){
        JsonObject tileData = new JsonObject();
        tileData.addProperty(FIELD_BIOME,biome().toString());
        tileData.addProperty(FIELD_TYPE,type());
        tileData.addProperty(FIELD_MAX_Y,maxY());
        tileData.addProperty(FIELD_BLOCK,material().key().toString());
        return tileData;
    }
    
    // just using this instead of an enum for convenience
    public static final class TileType {
        final public static String DEFAULT = "basic";
        final public static String VILLAGE = "village";
        final public static String OUTPOST = "pillager_outpost";
        final public static String WATER = "water";
    }
}
