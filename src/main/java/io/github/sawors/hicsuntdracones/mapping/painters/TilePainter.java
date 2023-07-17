package io.github.sawors.hicsuntdracones.mapping.painters;

import io.github.sawors.hicsuntdracones.mapping.WorldTile;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public abstract class TilePainter {
    // TODO : add this to the config ↴
    boolean paintUnderSeaLevel = false;
    final World world;
    
    public abstract @NotNull Color renderTile(WorldTile tile);
    
    public TilePainter(World world) {this.world = world;}
    
    public void shouldPaintUnderSeaLevel(boolean shouldPaint){
        this.paintUnderSeaLevel = shouldPaint;
    }
}
