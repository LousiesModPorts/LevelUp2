package levelup2.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.UUID;

public interface IProcessor
{
    void extraProcessing(PlayerEntity player);

    void setUUID(UUID placer);

    PlayerEntity getPlayerFromUUID();

    CompoundNBT writeToNBT(CompoundNBT tag);

    void readFromNBT(CompoundNBT tag);
}
