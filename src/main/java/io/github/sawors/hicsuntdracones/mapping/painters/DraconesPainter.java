package io.github.sawors.hicsuntdracones.mapping.painters;

import io.github.sawors.hicsuntdracones.mapping.WorldTile;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class DraconesPainter extends TilePainter{
    public DraconesPainter(World world) {
        super(world);
    }
    
    @Override
    public @NotNull Color renderTile(WorldTile tile) {
        return null;
    }
}
