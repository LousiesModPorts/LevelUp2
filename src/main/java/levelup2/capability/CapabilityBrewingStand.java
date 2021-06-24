package levelup2.capability;

import levelup2.skills.SkillRegistry;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.util.ResourceLocation;

public class CapabilityBrewingStand extends PlayerCapability.CapabilityProcessorDefault
{
    private static final ResourceLocation BREWING = new ResourceLocation("levelup", "brewingspeed");

    public CapabilityBrewingStand(BrewingStandTileEntity stand)
    {
        super(stand);
    }

    @Override
    public void extraProcessing(PlayerEntity player)
    {
        if (SkillRegistry.getSkillFromName(BREWING) == null || !SkillRegistry.getSkillFromName(BREWING).isActive() || !SkillRegistry.getSkillFromName(BREWING).isEnabled())
        {
            return;
        }

        if (tile != null)
        {
            if (tile instanceof BrewingStandTileEntity)
            {
                BrewingStandTileEntity stand = (BrewingStandTileEntity) tile;
                if (stand.getField(0) > 0)
                {
                    int bonus = SkillRegistry.getSkillLevel(player, BREWING);
                    if (bonus > 0)
                    {
                        int time = player.getRandom().nextInt(bonus + 1);

                        if (time > 0 && stand.getField(0) - time > 0)
                        {
                            stand.setField(0, stand.getField(0) - time);
                        }
                    }
                }
            }
        }
    }
}