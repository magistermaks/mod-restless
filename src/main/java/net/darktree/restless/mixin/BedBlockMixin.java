package net.darktree.restless.mixin;

import net.minecraft.block.BedBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public abstract class BedBlockMixin {

    /**
     * Injects before <code>return false</code> in <code>BedBlock#isFree</code>, to make
     * the "block.minecraft.bed.occupied" message from <code>BedBlock#onUse</code> unreachable
     */
    @Inject(at = @At(value = "RETURN", ordinal = 0), method="isFree(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z", cancellable = true)
    private void isFree(World world, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue(true);
    }

}
