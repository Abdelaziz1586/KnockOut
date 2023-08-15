package org.pebbleprojects.knockout.handlers;

import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class ParticleHandler {

    public static ParticleHandler INSTANCE;

    public ParticleHandler() {
        INSTANCE = this;
    }

    public void playParticle(final Location location) {
        new ParticleBuilder(ParticleEffect.WATER_BUBBLE, location)
                .setOffsetY(1f)
                .setSpeed(0.1f).display();
    }

}
