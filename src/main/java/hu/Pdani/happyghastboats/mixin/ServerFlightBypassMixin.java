package hu.Pdani.happyghastboats.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerFlightBypassMixin {

    @Shadow public ServerPlayerEntity player;

    @Inject(method = "tick", at = @At("HEAD"))
    private void grantFlightPermit(CallbackInfo ci) {
        Entity vehicle = player.getVehicle();

        // Check if the player is in a boat that is leashed
        if (vehicle instanceof AbstractBoatEntity boat) {
            if (boat.isLeashed()) {
                // This resets the "floating ticks" counter to 0 every tick
                // effectively disabling the "flying" kick.
                this.floatingTicks = 0;
                this.vehicleFloatingTicks = 0;
            }
        }
    }

    // These shadows let us access the private "kick" counters
    @Shadow private int floatingTicks;
    @Shadow private int vehicleFloatingTicks;
}