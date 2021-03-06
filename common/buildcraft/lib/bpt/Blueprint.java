/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.bpt;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.bpt.BlueprintAPI;
import buildcraft.api.bpt.SchematicBlock;
import buildcraft.api.bpt.SchematicException;
import buildcraft.api.bpt.SchematicFactoryWorldBlock;

import buildcraft.lib.bpt.builder.SchematicEntityOffset;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;

public class Blueprint extends BlueprintBase {
    private SchematicBlock[][][] contentBlocks;
    private List<SchematicEntityOffset> contentEntities;

    private Blueprint(BlockPos size, BlockPos offset) {
        super(size, offset);
        contentBlocks = new SchematicBlock[size.getX()][size.getY()][size.getZ()];
        contentEntities = new ArrayList<>();
    }

    public Blueprint(SchematicBlock[][][] blocks, List<SchematicEntityOffset> entities) {
        this(new BlockPos(0, 0, 0), blocks, entities);
    }

    public Blueprint(BlockPos offset, SchematicBlock[][][] blocks, List<SchematicEntityOffset> entities) {
        super(new BlockPos(blocks.length, blocks[0].length, blocks[0][0].length), offset);
        contentBlocks = blocks;
        if (entities == null) {
            contentEntities = new ArrayList<>();
        } else {
            contentEntities = new ArrayList<>(entities);
        }
    }

    public Blueprint(World world, BlockPos from, BlockPos size) throws SchematicException {
        this(size, new BlockPos(0, 0, 0));
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    BlockPos pos = from.add(x, y, z);
                    Block block = world.getBlockState(pos).getBlock();
                    SchematicFactoryWorldBlock factory = BlueprintAPI.getWorldBlockSchematic(block);
                    SchematicBlock schema = factory == null ? null : factory.createFromWorld(world, pos);
                    if (schema != null) {
                        contentBlocks[x][y][z] = schema;
                    } else {
                        contentBlocks[x][y][z] = null;//SchematicAir.INSTANCE;
                    }
                }
            }
        }
        // TODO: Entities
    }

    public Blueprint(NBTTagCompound nbt) {
        super(nbt);
        contentBlocks = new SchematicBlock[size.getX()][size.getY()][size.getZ()];
        contentEntities = new ArrayList<>();
        NBTTagList blocks = nbt.getTagList("blocks", Constants.NBT.TAG_COMPOUND);
        int index = 0;
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    NBTTagCompound comp = blocks.getCompoundTagAt(index);
                    index++;
                    try {
                        contentBlocks[x][y][z] = BlueprintAPI.deserializeSchematicBlock(comp);
                        if (contentBlocks[x][y][z] == null) {
                            throw new SchematicException("Null schematic from " + comp);
                        }
                    } catch (SchematicException e) {
                        throw new Error(e);
                    }
                }
            }
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        NBTTagList list = new NBTTagList();
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    SchematicBlock block = contentBlocks[x][y][z];
                    list.appendTag(block.serializeNBT());
                }
            }
        }
        nbt.setTag("blocks", list);
        return nbt;
    }

    @Override
    protected void rotateContentsBy(Axis axis, Rotation rotation) {
        BlockPos oldSize = this.size;
        BlockPos newSize = VecUtil.absolute(PositionUtil.rotatePos(oldSize, axis, rotation));
        SchematicBlock[][][] newContentBlocks = new SchematicBlock[newSize.getX()][newSize.getY()][newSize.getZ()];
        Box to = new Box(BlockPos.ORIGIN, newSize.add(-1, -1, -1));
        BlockPos newMax = PositionUtil.rotatePos(size.add(-1, -1, -1), axis, rotation);
        BlockPos arrayOffset = to.closestInsideTo(newMax).subtract(newMax);

        for (int x = 0; x < contentBlocks.length; x++) {
            for (int y = 0; y < contentBlocks[x].length; y++) {
                for (int z = 0; z < contentBlocks[x][y].length; z++) {
                    SchematicBlock schematic = contentBlocks[x][y][z];
                    schematic.rotate(axis, rotation);
                    BlockPos original = new BlockPos(x, y, z);
                    BlockPos rotated = PositionUtil.rotatePos(original, axis, rotation);
                    rotated = rotated.add(arrayOffset);
                    newContentBlocks[rotated.getX()][rotated.getY()][rotated.getZ()] = schematic;
                }
            }
        }

        contentBlocks = newContentBlocks;

        for (SchematicEntityOffset schematic : contentEntities) {
            schematic.rotate(axis, rotation, oldSize);
        }
    }

    @Override
    public void mirror(Axis axis) {
        SchematicBlock[][][] newContentBlocks = new SchematicBlock[size.getX()][size.getY()][size.getZ()];

        for (int x = 0; x < contentBlocks.length; x++) {
            for (int y = 0; y < contentBlocks[x].length; y++) {
                for (int z = 0; z < contentBlocks[x][y].length; z++) {
                    SchematicBlock schematic = contentBlocks[x][y][z];
                    schematic.mirror(axis);
                    BlockPos mirrored = new BlockPos(x, y, z);
                    int value = VecUtil.getValue(size, axis) - 1 - VecUtil.getValue(mirrored, axis);
                    mirrored = VecUtil.replaceValue(mirrored, axis, value);
                    newContentBlocks[mirrored.getX()][mirrored.getY()][mirrored.getZ()] = schematic;
                }
            }
        }

        contentBlocks = newContentBlocks;

        for (SchematicEntityOffset schematic : contentEntities) {
            schematic.mirror(axis, size);
        }
    }

    public SchematicBlock getSchematicAt(BlockPos pos) {
        return contentBlocks[pos.getX()][pos.getY()][pos.getZ()];
    }
}
