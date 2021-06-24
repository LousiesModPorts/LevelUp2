package levelup2.capability;

import levelup2.api.IProcessor;
import levelup2.player.IPlayerClass;
import levelup2.util.Library;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import java.util.UUID;

public class PlayerCapability
{
    @CapabilityInject(IPlayerClass.class)
    public static Capability<IPlayerClass> PLAYER_CLASS = null;

    @CapabilityInject(IProcessor.class)
    public static Capability<IProcessor> MACHINE_PROCESSING = null;

    public static class CapabilityPlayerClass<T extends IPlayerClass> implements Capability.IStorage<IPlayerClass>
    {
        @Override
        public INBT writeNBT(Capability<IPlayerClass> capability, IPlayerClass player, Direction side)
        {
            return player.saveNBTData(new CompoundNBT());
        }

        @Override
        public void readNBT(Capability<IPlayerClass> capability, IPlayerClass player, Direction side, INBT nbt)
        {
            player.loadNBTData((CompoundNBT)nbt);
        }
    }

    public static class CapabilityProcessorClass<T extends IProcessor> implements Capability.IStorage<IProcessor>
    {
        @Override
        public INBT writeNBT(Capability<IProcessor> capability, IProcessor process, Direction side)
        {
            return process.writeToNBT(new CompoundNBT());
        }

        @Override
        public void readNBT(Capability<IProcessor> capability, IProcessor process, Direction side, INBT nbt)
        {
            process.readFromNBT((CompoundNBT)nbt);
        }
    }

    public static class CapabilityProcessorDefault implements IProcessor
    {
        private PlayerEntity player;
        protected UUID playerUUID;
        protected TileEntity tile;

        public CapabilityProcessorDefault(TileEntity entity)
        {
            tile = entity;
        }

        @Override
        public void setUUID(UUID playerUUID)
        {
            this.playerUUID = playerUUID;
        }

        @Override
        public void readFromNBT(CompoundNBT tag)
        {
            if (tag.contains("player_uuid"))
            {
                playerUUID = UUID.fromString(tag.getString("player_uuid"));
                player = Library.getPlayerFromUUID(playerUUID);
            }
        }

        @Override
        public CompoundNBT writeToNBT(CompoundNBT tag)
        {
            if (playerUUID != null)
            {
                tag.putString("player_uuid", playerUUID.toString());
            }

            return tag;
        }

        @Override
        public PlayerEntity getPlayerFromUUID()
        {
            if (playerUUID == null)
            {
                if (player != null)
                {
                    player = null;
                }

                return null;
            }

            if (player == null)
            {
                player = Library.getPlayerFromUUID(playerUUID);
            }
            return player;
        }

        @Override
        public void extraProcessing(PlayerEntity player) {}
    }
}
