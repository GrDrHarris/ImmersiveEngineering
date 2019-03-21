/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.wires.Connection;
import blusunrize.immersiveengineering.api.energy.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TileEntityRazorWire extends TileEntityImmersiveConnectable implements IDirectionalTile, IAdvancedCollisionBounds,
		IOBJModelCallback<IBlockState>, EnergyConnector
{
	public EnumFacing facing = EnumFacing.NORTH;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = EnumFacing.byIndex(nbt.getInteger("facing"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing.ordinal());
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 2;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return true;
	}

	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		if(entity instanceof EntityLivingBase)
		{
			entity.motionX *= 0.2D;
			entity.motionZ *= 0.2D;
			applyDamage((EntityLivingBase)entity);
		}
	}

	public static void applyDamage(EntityLivingBase entity)
	{
		int protection = (!entity.getItemStackFromSlot(EntityEquipmentSlot.FEET).isEmpty()?1: 0)+(!entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS).isEmpty()?1: 0);
		float dmg = protection==2?.5f: protection==1?1: 1.5f;
		entity.attackEntityFrom(IEDamageSources.razorWire, dmg);
	}

	@Override
	public float[] getBlockBounds()
	{
		return null;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		boolean wallL = renderWall(true);
		boolean wallR = renderWall(false);
		if((!isOnGround()&&!isStacked())||!(wallL||wallR))
			return Collections.singletonList(null);
		List<AxisAlignedBB> list = new ArrayList<>(wallL&&wallR?2: 1);
		if(wallL)
			list.add(new AxisAlignedBB(facing==EnumFacing.SOUTH?.8125: 0, 0, facing==EnumFacing.WEST?.8125: 0, facing==EnumFacing.NORTH?.1875: 1, 1, facing==EnumFacing.EAST?.1875: 1).offset(getPos()));
		if(wallR)
			list.add(new AxisAlignedBB(facing==EnumFacing.NORTH?.8125: 0, 0, facing==EnumFacing.EAST?.8125: 0, facing==EnumFacing.SOUTH?.1875: 1, 1, facing==EnumFacing.WEST?.1875: 1).offset(getPos()));
		return list;
	}

	private boolean renderWall(boolean left)
	{
		EnumFacing dir = left?facing.rotateY(): facing.rotateYCCW();
		BlockPos neighbourPos = getPos().offset(dir, -1);
		if(!world.isBlockLoaded(neighbourPos))
			return true;
		if(world.getTileEntity(neighbourPos) instanceof TileEntityRazorWire)
			return false;
		IBlockState neighbour = world.getBlockState(neighbourPos);
		return !neighbour.isSideSolid(world, neighbourPos, dir);
	}

	private boolean isOnGround()
	{
		BlockPos down = getPos().down();
		return world.getBlockState(down).isSideSolid(world, down, EnumFacing.UP);
	}

	private boolean isStacked()
	{
		BlockPos down = getPos().down();
		TileEntity te = world.getTileEntity(down);
		if(te instanceof TileEntityRazorWire)
			return ((TileEntityRazorWire)te).isOnGround();
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldRenderGroup(IBlockState object, String group)
	{
		if(group==null)
			return false;
		boolean stack = isStacked();
		if(!stack&&!isOnGround())
			return !group.startsWith("wood");
		if(group.startsWith("wood")&&!(group.endsWith("inverted")==stack))
			return false;
		if(group.startsWith("wood_left"))
			return renderWall(true);
		else if("wire_left".equals(group)||"barbs_left".equals(group))
			return !renderWall(true);
		else if(group.startsWith("wood_right"))
			return renderWall(false);
		else if("wire_right".equals(group)||"barbs_right".equals(group))
			return !renderWall(false);
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getCacheKey(IBlockState object)
	{
		boolean stack = isStacked();
		if(!stack&&!isOnGround())
			return "default";
		return (renderWall(true)?"L": " ")+(renderWall(false)?"R": " ")+(stack?"_stack": "");
	}

	@Override
	protected boolean canTakeLV()
	{
		return true;
	}

	@Override
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		BlockPos other = con.getOtherEnd(here).getPosition();
		int xDif = other.getX()-pos.getX();
		int yDif = other.getY()-pos.getY();
		int zDif = other.getZ()-pos.getZ();
		boolean wallL = renderWall(true);
		boolean wallR = renderWall(false);
		if(!isOnGround()||!(wallL||wallR))
		{
			if(yDif > 0)
				return new Vec3d(facing.getXOffset()!=0?.5: xDif < 0?.40625: .59375, .9375, facing.getZOffset()!=0?.5: zDif < 0?.40625: .59375);
			else
			{
				boolean right = facing.rotateY().getAxisDirection().getOffset()==Math.copySign(1, facing.getXOffset()!=0?zDif: xDif);
				int faceX = facing.getXOffset();
				int faceZ = facing.getZOffset();
				return new Vec3d(faceX!=0?.5+(right?0: faceX*.1875): (xDif < 0?0: 1), .046875, faceZ!=0?.5+(right?0: faceZ*.1875): (zDif < 0?0: 1));
			}
		}
		else
		{
			boolean wallN = facing==EnumFacing.NORTH||facing==EnumFacing.EAST?wallL: wallR;
			return new Vec3d(facing.getXOffset()!=0?.5: xDif < 0&&wallN?.125: .875, .9375, facing.getZOffset()!=0?.5: zDif < 0&&wallN?.125: .875);
		}
	}

	@Override
	public boolean isSource(ConnectionPoint cp)
	{
		return false;
	}

	@Override
	public boolean isSink(ConnectionPoint cp)
	{
		return true;
	}

	@Override
	public int getRequestedEnergy()
	{
		return 64;
	}

	@Override
	public void insertEnergy(int amount)
	{
		int maxReach = amount/8;
		int widthP = 0;
		boolean connectP = true;
		int widthN = 0;
		boolean connectN = true;
		EnumFacing dir = facing.rotateY();
		if(dir.getAxisDirection()==AxisDirection.NEGATIVE)
			dir = dir.getOpposite();
		for(int i = 1; i <= maxReach; i++)
		{
			BlockPos posP = getPos().offset(dir, i);
			if(connectP&&world.isBlockLoaded(posP)&&world.getTileEntity(posP) instanceof TileEntityRazorWire)
				widthP++;
			else
				connectP = false;
			BlockPos posN = getPos().offset(dir, -i);
			if(connectN&&world.isBlockLoaded(posN)&&world.getTileEntity(posN) instanceof TileEntityRazorWire)
				widthN++;
			else
				connectN = false;
		}
		AxisAlignedBB aabb = new AxisAlignedBB(getPos().add(facing.getAxis()==Axis.Z?-widthN: 0, 0, facing.getAxis()==Axis.X?-widthN: 0), getPos().add(facing.getAxis()==Axis.Z?1+widthP: 1, 1, facing.getAxis()==Axis.X?1+widthP: 1));
		List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
		for(EntityLivingBase ent : entities)
			ent.attackEntityFrom(IEDamageSources.razorShock, 2);
	}
}