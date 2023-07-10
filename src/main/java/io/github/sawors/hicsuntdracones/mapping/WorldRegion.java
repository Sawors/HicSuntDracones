package io.github.sawors.hicsuntdracones.mapping;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;

public record WorldRegion(int x, int z, ChunkSnapshot[] chunks, World world) {
    
    public String getFileName() {
        return "r." + x + "." + z + ".mca";
    }
    
    public static Pair<Integer,Integer> getRegionForCoordinates(double locationX, double locationZ){
        return new IntIntImmutablePair(
                Math.floorDiv((int) locationX,32*16),
                Math.floorDiv((int) locationZ,32*16)
        );
    }
}
