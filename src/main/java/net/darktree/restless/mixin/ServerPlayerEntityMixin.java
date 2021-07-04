package net.darktree.restless.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

import static net.minecraft.block.BedBlock.findWakeUpPosition;

@Mixin(value = ServerPlayerEntity.class, priority = 2000)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Shadow private RegistryKey<World> spawnPointDimension;
    @Shadow private BlockPos spawnPointPosition;
    @Shadow private boolean spawnPointSet;
    @Shadow private float spawnAngle;
    @Shadow public abstract void sendMessage(Text message, boolean actionBar);

    private void setSpawnPoint(RegistryKey<World> dimension, BlockPos pos, float angle ) {

        if( !pos.equals(spawnPointPosition) || !dimension.equals(spawnPointDimension) ) {
            sendMessage( new TranslatableText("block.minecraft.set_spawn"), true );
        }

        spawnPointPosition = pos;
        spawnPointDimension = world.getRegistryKey();
        spawnAngle = angle;
        spawnPointSet = true;
    }

    /**
     * Overwrite <code>ServerPlayerEntity#trySleep</code> to disable sleeping.
     *
     * @reason to delete most checks and a call to <code>super.trySleep</code>.
     * @author magistermaks
     */
    @Overwrite
    public Either<SleepFailureReason, Unit> trySleep(BlockPos pos) {

        if ( isSleeping() || !isAlive() || pos == null ) {
            return Either.left(PlayerEntity.SleepFailureReason.OTHER_PROBLEM);
        }

        if ( !world.getDimension().isNatural() ) {
            return Either.left(PlayerEntity.SleepFailureReason.NOT_POSSIBLE_HERE);
        }

        if ( findWakeUpPosition(EntityType.PLAYER, world, pos, 0).equals( Optional.empty() ) ) {
            return Either.left(PlayerEntity.SleepFailureReason.OBSTRUCTED);
        }

        resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
        setSpawnPoint(world.getRegistryKey(), pos, headYaw);
        return Either.right(Unit.INSTANCE);
    }

}
