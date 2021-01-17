package org.dimdev.jeid.mixin.core;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;
import java.io.IOException;

@Mixin(PacketBuffer.class)
public abstract class MixinPacketBuffer {
    @Shadow public abstract byte readByte();

    @Shadow public abstract short readShort();

    @Shadow public abstract int readVarInt();

    @Shadow @Nullable public abstract NBTTagCompound readCompoundTag() throws IOException;

    @Shadow public abstract PacketBuffer writeVarInt(int input);

    @Redirect(method = "writeItemStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketBuffer;writeShort(I)Lio/netty/buffer/ByteBuf;", ordinal = 0))
    private ByteBuf writeIntItemId(PacketBuffer packetBuffer, int p_writeShort_1_) {
        return this.writeVarInt(p_writeShort_1_);
    }

    @Redirect(method = "writeItemStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketBuffer;writeShort(I)Lio/netty/buffer/ByteBuf;", ordinal = 1))
    private ByteBuf writeIntItemId1(PacketBuffer packetBuffer, int p_writeShort_1_) {
        return this.writeVarInt(p_writeShort_1_);
    }

    /**
     * @author Rongmario
     * @reason Fuck injects.
     */
    @Overwrite
    public ItemStack readItemStack() throws IOException {
        int i = this.readVarInt();
        if (i < 0) {
            return ItemStack.EMPTY;
        } else {
            int j = this.readByte();
            int k = this.readShort();
            ItemStack itemstack = new ItemStack(Item.getItemById(i), j, k);
            itemstack.getItem().readNBTShareTag(itemstack, this.readCompoundTag());
            return itemstack;
        }
    }

}
