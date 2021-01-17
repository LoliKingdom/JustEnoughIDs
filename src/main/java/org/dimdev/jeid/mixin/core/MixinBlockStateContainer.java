package org.dimdev.jeid.mixin.core;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BitArray;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.IBlockStatePalette;
import net.minecraft.world.chunk.NibbleArray;
import org.dimdev.jeid.INewBlockStateContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Mixin(BlockStateContainer.class)
public abstract class MixinBlockStateContainer implements INewBlockStateContainer {
    @Shadow protected abstract IBlockState get(int index);
    @Shadow @SuppressWarnings("unused") protected BitArray storage;
    @Shadow @SuppressWarnings("unused") protected IBlockStatePalette palette;
    @Shadow protected abstract void set(int index, IBlockState state);
    @Shadow @SuppressWarnings("unused") protected abstract void setBits(int bitsIn);

    private int[] temporaryPalette; // index -> state id

    @Override
    public int[] getTemporaryPalette() {
        return temporaryPalette;
    }

    @Override
    public void setTemporaryPalette(int[] temporaryPalette) {
        this.temporaryPalette = temporaryPalette;
    }

    /**
     * @author Rongmario
     * @reason Fuck Injects.
     */
    @Overwrite
    @Nullable
    public NibbleArray getDataForNBT(byte[] blockIds, NibbleArray data) {
        Object2IntMap<IBlockState> stateIDMap = new Object2IntOpenHashMap<>();
        int j = 0;
        for (int i = 0; i < 4096; ++i) {
            IBlockState state = get(i);
            Integer paletteID = stateIDMap.get(state);
            if (paletteID == null) {
                paletteID = j++;
                stateIDMap.put(state, paletteID);
            }
            int x = i & 15;
            int y = i >> 8 & 15;
            int z = i >> 4 & 15;
            blockIds[i] = (byte) (paletteID >> 4 & 255);
            data.set(x, y, z, paletteID & 15);
        }
        this.temporaryPalette = new int[j];
        stateIDMap.forEach((s, id) -> this.temporaryPalette[id] = Block.BLOCK_STATE_IDS.get(s));
        return null;
    }

    /**
     * @reason If this BlockStateContainer is saved in JustEnoughIDs format, treat
     * the "Blocks" and "Data" arrays as palette IDs.
     */
    @SuppressWarnings("deprecation")
    @Inject(method = "setDataFromNBT", at = @At("HEAD"), cancellable = true)
    private void newSetDataFromNBT(byte[] blockIds, NibbleArray data, NibbleArray blockIdExtension, CallbackInfo ci) {
        if (temporaryPalette == null) return; // Read containers in in palette format only if the container has a palette (has a palette)

        for (int index = 0; index < 4096; ++index) {
            int x = index & 15;
            int y = index >> 8 & 15;
            int z = index >> 4 & 15;
            int paletteID = (blockIds[index] & 255) << 4 | data.get(x, y, z);

            set(index, Block.BLOCK_STATE_IDS.getByValue(temporaryPalette[paletteID]));
        }

        temporaryPalette = null;
        ci.cancel();
    }
}
