package me.karven.client;

import net.minecraft.world.phys.Vec3;

public class Values {
    public final static double MAX_COORDINATE_VALUE = 30000500;


    public static boolean checkLimit(final double... values) {
        for (final double value : values) {
            if (value > MAX_COORDINATE_VALUE) return false;
        }
        return true;
    }

    public static boolean checkLimit(final Vec3 position) {
        if (!checkLimit(position.x(), position.z())) return false;
        return position.y() < 10000;
    }
}
