package levelup2.event;

import levelup2.api.ICharacterClass;
import levelup2.api.IPlayerSkill;
import levelup2.capability.PlayerCapability;
import levelup2.config.LevelUpConfig;
import levelup2.network.SkillPacketHandler;
import levelup2.player.IPlayerClass;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class CapabilityEventHandler
{
    @SubscribeEvent
    public static void onPlayerEntersWorld(AttachCapabilitiesEvent<Entity> evt)
    {
        if (evt.getObject() instanceof PlayerEntity)
        {
            evt.addCapability(Library.SKILL_LOCATION, new ICapabilitySerializable<CompoundNBT>()
            {
                IPlayerClass instance = PlayerCapability.PLAYER_CLASS.getDefaultInstance();

                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side)
                {

                    return capability == PlayerCapability.PLAYER_CLASS ? PlayerCapability.PLAYER_CLASS.<T>cast(instance) : null;
                }

                @Override
                public CompoundNBT serializeNBT() 
                {
                    return ((CompoundNBT)PlayerCapability.PLAYER_CLASS.getStorage().writeNBT(PlayerCapability.PLAYER_CLASS, instance, null));
                }

                @Override
                public void deserializeNBT(CompoundNBT tag) 
                {
                    PlayerCapability.PLAYER_CLASS.getStorage().readNBT(PlayerCapability.PLAYER_CLASS, instance, null, tag);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone evt)
    {
        if (!evt.isWasDeath() || !LevelUpConfig.resetClassOnDeath)
        {
            CompoundNBT data = new CompoundNBT();
            SkillRegistry.getPlayer(evt.getOriginal()).saveNBTData(data);
            SkillRegistry.getPlayer(evt.getPlayer()).loadNBTData(data);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent evt)
    {
        SkillRegistry.loadPlayer(evt.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent evt)
    {
        SkillRegistry.loadPlayer(evt.getPlayer());
    }

    private static final String BOOK_TAG = "levelup:bookspawn";

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent evt)
    {
        if (evt.getPlayer() instanceof ServerPlayerEntity)
        {
            spawnBook(evt.getPlayer());
            SkillRegistry.loadPlayer(evt.getPlayer());
            SkillPacketHandler.configChannel.sendTo(SkillPacketHandler.getConfigPacket(LevelUpConfig.getServerProperties()), (ServerPlayerEntity)evt.getPlayer());

            for (ResourceLocation loc : SkillRegistry.getSkills().keySet())
            {
                IPlayerSkill skill = SkillRegistry.getSkillFromName(loc);
                SkillPacketHandler.propertyChannel.sendTo(SkillPacketHandler.getPropertyPackets(skill), (ServerPlayerEntity)evt.getPlayer());
            }

            for (ResourceLocation loc : SkillRegistry.getClasses().keySet())
            {
                ICharacterClass cl = SkillRegistry.getClassFromName(loc);
                SkillPacketHandler.classChannel.sendTo(SkillPacketHandler.getClassPackets(cl), (ServerPlayerEntity)evt.getPlayer());
            }

            SkillPacketHandler.refreshChannel.sendTo(SkillPacketHandler.getRefreshPacket(), (ServerPlayerEntity)evt.getPlayer());
        }
    }

    private static void spawnBook(ServerPlayerEntity player)
    {
        if (LevelUpConfig.giveSkillBook)
        {
            CompoundNBT playerData = player.getEntityData();
            CompoundNBT data = getTag(playerData, PlayerEntity.PERSISTED_NBT_TAG);

            if (!data.getBoolean(BOOK_TAG))
            {
                ItemStack book = new ItemStack(SkillRegistry.skillBook);

                if (!player.addItemStackToInventory(book))
                {
                    player.dropItem(book, true);
                }

                data.setBoolean(BOOK_TAG, true);
                playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
            }
        }
    }

    private static CompoundNBT getTag(CompoundNBT base, String tag)
    {
        if (base == null)
        {
            return new CompoundNBT();
        }

        return base.getCompound(tag);
    }

    public static double getDivisor(ResourceLocation skill)
    {
        IPlayerSkill sk = SkillRegistry.getSkillFromName(skill);

        if (sk != null)
        {
            return SkillRegistry.getProperty(sk).getDivisor();
        }

        return 1;
    }
}
