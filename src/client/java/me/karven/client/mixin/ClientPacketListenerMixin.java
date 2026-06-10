package me.karven.client.mixin;

import me.karven.client.Values;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(at = @At("HEAD"), method = "handleParticleEvent", cancellable = true)
    public void handleParticleEvent(final ClientboundLevelParticlesPacket packet, final CallbackInfo ci) {
        if (packet.getCount() > 10000) {
            ci.cancel();
            return;
        }
        if (!Values.checkLimit(packet.getX(), packet.getZ(), packet.getXDist(), packet.getZDist())) {
            ci.cancel();
            return;
        }

        if (packet.getMaxSpeed() > 1000 || packet.getY() > 10000 || packet.getYDist() > 10000)
            ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "handleExplosion", cancellable = true)
    public void handleExplosion(final ClientboundExplodePacket packet, final CallbackInfo ci) {
        if (!Values.checkLimit(packet.center())) {
            ci.cancel();
            return;
        }

        if (packet.playerKnockback().isPresent() && !Values.checkLimit(packet.playerKnockback().get())) {
            ci.cancel();
            return;
        }

        if (packet.radius() > 1000 || packet.blockCount() > 1000)
            ci.cancel();

        // should also handle block particles, but I have no clue how that thing works
    }

    @Inject(at = @At("HEAD"), method = "handleMovePlayer", cancellable = true)
    public void handleMovePlayer(final ClientboundPlayerPositionPacket packet, final CallbackInfo ci) {
        if (Values.checkLimit(packet.change().position()) && Values.checkLimit(packet.change().deltaMovement()))
            return;

        ci.cancel();
        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer player = minecraft.player;
        final ClientPacketListener connection = minecraft.getConnection();
        if (connection == null || player == null) return;
        connection.send(new ServerboundAcceptTeleportationPacket(packet.id()));
        connection.send(new ServerboundMovePlayerPacket.PosRot(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), false, false));
    }
}
