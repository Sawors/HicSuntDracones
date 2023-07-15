package io.github.sawors.hicsuntdracones.mapping.renderers;

import io.github.sawors.hicsuntdracones.mapping.WorldTile;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public abstract class TileRenderer {
    // TODO : add this to the config ↴
    boolean renderUnderSeaLevel = false;
    final World world;
    
    public abstract @NotNull Color renderTile(WorldTile tile);
    
    public TileRenderer(World world) {this.world = world;}
    
    public void shouldRenderUnderSeaLevel(boolean shouldRender){
        this.renderUnderSeaLevel = shouldRender;
    }
}
