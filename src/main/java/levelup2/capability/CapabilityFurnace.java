package levelup2.capability;

import levelup2.api.IPlayerSkill;
import levelup2.config.LevelUpConfig;
import levelup2.skills.SkillRegistry;
import levelup2.util.SmeltingBlacklist;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.fixes.FurnaceRecipes;
import net.minecraft.util.math.BlockPos;

public class CapabilityFurnace extends PlayerCapability.CapabilityProcessorDefault
{
    private static final ResourceLocation FURNACESPEED = new ResourceLocation("levelup", "furnacespeed");
    private static final ResourceLocation FURNACEBONUS = new ResourceLocation("levelup", "furnacebonus");
    public CapabilityFurnace(FurnaceTileEntity tile)
    {
        super(tile);
    }

    @Override
    public void extraProcessing(PlayerEntity player)
    {
        if (tile != null)
        {
            FurnaceTileEntity furnace = (FurnaceTileEntity) tile;

            if (furnace.isBurning())
            {
                if (furnace.canSmelt())
                {
                    ItemStack stack = furnace.getItem(0);

                    if (!stack.isEmpty())
                    {
                        int bonus = SkillRegistry.getSkillLevel(player, FURNACESPEED);

                        if (bonus > 0 || !isSkillActive("levelup:furnacespeed"))
                        {
                            int time = player.getRandom().nextInt(bonus + 1);

                            if (isSkillActive("levelup:furnacespeed") && time > 0 && furnace.getField(2) + time < furnace.getField(3))
                            {
                                furnace.setField(2, furnace.getField(2) + time);
                            }

                            if (furnace.getField(2) > furnace.getField(3) - 2 && isSkillActive("levelup:furnacebonus"))
                            {
                                bonus = SkillRegistry.getSkillLevel(player, FURNACEBONUS);

                                if (bonus > 0)
                                {
                                    if (isDoublingValid(furnace) && player.getRandom().nextFloat() < bonus / 40F)
                                    {
                                        ItemStack result = FurnaceRecipes.instance().getSmeltingResult(stack).copy();

                                        if (!LevelUpConfig.furnaceEjection)
                                        {
                                            if (furnace.getItem(2).isEmpty())
                                            {
                                                furnace.setItem(2, result);
                                            }

                                            else
                                            {
                                                ItemStack product = furnace.getItem(2);
                                                if (ItemStack.isSame(result, product))
                                                {
                                                    if (product.getCount() + (result.getCount() * 2) <= product.getMaxStackSize())
                                                    {
                                                        furnace.getItem(2).grow(result.getCount());
                                                    }
                                                }
                                            }
                                        }

                                        else
                                        {
                                            ejectExtraItem(result);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isDoublingValid(FurnaceTileEntity tile)
    {
        ItemStack smeltingItem = tile.getItem(0);
        return !FurnaceRecipes.instance().getSmeltingResult(smeltingItem).isEmpty() && !SmeltingBlacklist.contains(smeltingItem);
    }

    private void ejectExtraItem(ItemStack stack)
    {
        if (!stack.isEmpty())
        {
            if (tile.getBlockState().getBlock() == Blocks.FURNACE || tile.getBlockState().getBlock() == Blocks.LIT_FURNACE)
            {
                BlockState furnace = tile.getLevel().getBlockState(tile.getBlockPos());
                Direction facing = furnace.getValue(FurnaceBlock.FACING);
                BlockPos offset = tile.getBlockPos().relative(facing);
                ItemEntity item = new ItemEntity(tile.getLevel(), offset.getX() + 0.5D, offset.getY() + 0.5D, offset.getZ() + 0.5D, stack);
                tile.getLevel().addFreshEntity(item);
            }
        }
    }

    private boolean isSkillActive(String skill)
    {
        IPlayerSkill sk = SkillRegistry.getSkillFromName(new ResourceLocation(skill));

        if (sk != null)
        {
            return sk.isActive() && sk.isEnabled();
        }

        return false;
    }
}
