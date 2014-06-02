/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.BuildCraftSilicon;
import buildcraft.api.recipes.CraftingResult;
import buildcraft.api.recipes.IIntegrationRecipeFactory;
import buildcraft.core.ItemRobot;
import buildcraft.core.recipes.FlexibleRecipe;
import buildcraft.core.utils.NBTUtils;
import buildcraft.silicon.ItemRedstoneBoard;
import buildcraft.silicon.TileIntegrationTable;

public class RobotIntegrationRecipe extends FlexibleRecipe implements IIntegrationRecipeFactory {

	public RobotIntegrationRecipe(String id) {
		setContents(id, new ItemStack(BuildCraftSilicon.robotItem), 10000);
	}

	@Override
	public boolean isValidInputA(ItemStack inputA) {
		return inputA != null && inputA.getItem() instanceof ItemRobot;
	}

	@Override
	public boolean isValidInputB(ItemStack inputB) {
		return inputB != null && inputB.getItem() instanceof ItemRedstoneBoard;
	}

	@Override
	public CraftingResult craft(IInventory items, IFluidHandler fluids) {
		ItemStack inputA = items.decrStackSize(TileIntegrationTable.SLOT_INPUT_A, 1);
		ItemStack inputB = items.decrStackSize(TileIntegrationTable.SLOT_INPUT_B, 1);

		CraftingResult result = super.craft(items, fluids);

		if (result != null) {
			ItemStack robot = new ItemStack(BuildCraftSilicon.robotItem);

			NBTUtils.getItemData(robot).setTag("board", NBTUtils.getItemData(items.getStackInSlot(1)));

			result.crafted = robot;

			return result;
		} else {
			return null;
		}
	}
}
