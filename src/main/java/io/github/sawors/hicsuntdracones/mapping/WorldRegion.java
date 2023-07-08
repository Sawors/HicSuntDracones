package io.github.sawors.hicsuntdracones.mapping;

import org.bukkit.ChunkSnapshot;

public record WorldRegion(int x, int z, ChunkSnapshot[] data) {
    
    public String getFileName() {
        return "r." + x + "." + z + ".mca";
    }
}
