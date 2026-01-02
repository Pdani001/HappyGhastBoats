package hu.Pdani.happyghastboats.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBoatEntity.class)
public abstract class BoatPhysicsMixin extends Entity implements Leashable {

    @Shadow private boolean pressingLeft;
    @Shadow private boolean pressingRight;
    @Shadow private boolean pressingForward;
    @Shadow private boolean pressingBack;

    public BoatPhysicsMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void forceGondolaPhysics(CallbackInfo ci) {
        if (this.isLeashed() && this.hasPlayerRider()) {
            Entity holder = (Entity) this.getLeashHolder();
            if (holder instanceof HappyGhastEntity) {
                // 1. INPUT LOCK (Server-side)
                this.pressingLeft = false;
                this.pressingRight = false;
                this.pressingBack = false;
                this.pressingForward = false;

                // 2. THE ANCHOR POINT
                // 4.0 blocks down is usually the "sweet spot" for Ghasts
                double hangDistance = 5.0D;
                Vec3d targetPos = holder.getEntityPos().subtract(0, hangDistance, 0);
                Vec3d currentPos = this.getEntityPos();

                // Calculate the horizontal offset (X and Z only)
                double diffX = targetPos.x - currentPos.x;
                double diffZ = targetPos.z - currentPos.z;
                double diffY = targetPos.y - currentPos.y;

                // 3. AGGRESSIVE HORIZONTAL SNAPPING
                // We apply a very high multiplier to X and Z to keep it centered
                double horizontalSnap = 0.25D;
                double verticalSnap = 0.15D;

                // Apply velocity directly (bypassing standard friction)
                Vec3d newVel = new Vec3d(
                        diffX * horizontalSnap,
                        (diffY * verticalSnap) + 0.045D, // Anti-gravity constant
                        diffZ * horizontalSnap
                );

                // If the boat is moving too fast (swinging), we dampen it
                this.setVelocity(newVel.multiply(0.95D));

                // 4. ROTATION (Face Ghast's Direction)
                // Instead of facing velocity, we now face the same direction as the Ghast
                // This makes it feel like you are part of the same vehicle.
                float ghastYaw = holder.getYaw();
                this.setYaw(ghastYaw);
                this.lastYaw = ghastYaw;
                this.setHeadYaw(ghastYaw);

                // 5. SERVER SYNC
                this.setOnGround(true);
                this.fallDistance = 0;
                this.velocityDirty = true;
            }
        }
    }
}