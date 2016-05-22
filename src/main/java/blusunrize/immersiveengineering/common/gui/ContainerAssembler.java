package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerAssembler extends ContainerIEBase<TileEntityAssembler>
{
	public ContainerAssembler(InventoryPlayer inventoryPlayer, TileEntityAssembler tile)
	{
		super(inventoryPlayer, tile);
		this.tile=tile;
		for(int i=0; i<tile.patterns.length; i++)
		{
			this.tile.patterns[i].recalculateOutput();
			for(int j=0; j<9; j++)
			{
				int x = 9+ i*58 + (j%3)*18;
				int y = 7+ (j/3)*18;
				this.addSlotToContainer(new IESlot.Ghost(this, tile.patterns[i], j, x, y));
			}
			this.addSlotToContainer(new IESlot.Output(this, this.inv, 18+i, 27+i*58, 64));
		}
		for(int i=0; i<18; i++)
			this.addSlotToContainer(new Slot(this.inv, i, 13+(i%9)*18, 87+(i/9)*18));
		slotCount=21;
		
		for(int i=0; i<3; i++)
			for(int j=0; j<9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 13+j*18, 137+i*18));
		for(int i=0; i<9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 13+i*18, 195));
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		ItemStack stack = null;
		Slot slotObject = (Slot) inventorySlots.get(slot);

		if(slotObject != null && slotObject.getHasStack() && !(slotObject instanceof IESlot.Ghost))
		{
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();
			if(slot<48)
			{
				if(!this.mergeItemStack(stackInSlot, 48,(48+36), true))
					return null;
			}
			else
			{
				if(!this.mergeItemStack(stackInSlot, 30,48, false))
					return null;
			}

			if (stackInSlot.stackSize == 0)
				slotObject.putStack(null);
			else
				slotObject.onSlotChanged();

			if (stackInSlot.stackSize == stack.stackSize)
				return null;
			slotObject.onPickupFromSlot(player, stackInSlot);
		}
		return stack;
	}
}