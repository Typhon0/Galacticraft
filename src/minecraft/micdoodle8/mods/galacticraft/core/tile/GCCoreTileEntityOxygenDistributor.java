package micdoodle8.mods.galacticraft.core.tile;

import mekanism.api.EnumGas;
import mekanism.api.IGasAcceptor;
import mekanism.api.ITubeConnection;
import micdoodle8.mods.galacticraft.core.blocks.GCCoreBlocks;
import micdoodle8.mods.galacticraft.core.entities.GCCoreEntityOxygenBubble;
import micdoodle8.mods.galacticraft.core.oxygen.OxygenBubble;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.components.common.BasicComponents;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.item.ElectricItemHelper;
import universalelectricity.core.item.IItemElectric;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.network.IPacketReceiver;
import universalelectricity.prefab.network.PacketManager;
import universalelectricity.prefab.tile.TileEntityElectricityRunnable;

import com.google.common.io.ByteArrayDataInput;

/**
 * Copyright 2012-2013, micdoodle8
 *
 *  All rights reserved.
 *
 */
public class GCCoreTileEntityOxygenDistributor extends TileEntityElectricityRunnable implements IInventory, IPacketReceiver, IGasAcceptor, ITubeConnection, ISidedInventory
{
	public int power;
	public int lastPower;
	
    public boolean active;
	private ItemStack[] containingItems = new ItemStack[1];
   	
//   	public OxygenBubble bubble;
   	
	public static final double WATTS_PER_TICK = 300;

	private int playersUsing = 0;
	
	public static int timeSinceOxygenRequest;
	
	public GCCoreEntityOxygenBubble oxygenBubble;

    @Override
  	public void invalidate()
  	{
//    	if (this.bubble != null)
//    	{
//    		if (this.bubble.connectedDistributors.contains(this))
//    		{
//        		this.bubble.connectedDistributors.remove(this);
//    		}
//    		
//        	this.bubble.stopProducingOxygen();
//    	}
    	
    	if (this.oxygenBubble != null)
    	{
    		this.oxygenBubble.setDead();
    	}
    	
    	for (int x = (int) Math.floor(this.xCoord - this.power * 1.5); x < Math.ceil(this.xCoord + this.power * 1.5); x++)
    	{
        	for (int y = (int) Math.floor(this.yCoord - this.power * 1.5); y < Math.ceil(this.yCoord + this.power * 1.5); y++)
        	{
            	for (int z = (int) Math.floor(this.zCoord - this.power * 1.5); z < Math.ceil(this.zCoord + this.power * 1.5); z++)
            	{
            		TileEntity tile = this.worldObj.getBlockTileEntity(x, y, z);
            		
            		if (tile != null && tile instanceof GCCoreTileEntityBreathableAir)
            		{
        				tile.worldObj.setBlockToAir(tile.xCoord, tile.yCoord, tile.zCoord);
						tile.invalidate();
            		}
            		
            		if (tile != null && tile instanceof GCCoreTileEntityUnlitTorch)
            		{
            			tile.worldObj.setBlock(tile.xCoord, tile.yCoord, tile.zCoord, GCCoreBlocks.unlitTorch.blockID, 0, 3);
            		}
            	}
        	}
    	}
    	
    	super.invalidate();
  	}

    public double getDistanceFromServer(double par1, double par3, double par5)
    {
        double d3 = this.xCoord + 0.5D - par1;
        double d4 = this.yCoord + 0.5D - par3;
        double d5 = this.zCoord + 0.5D - par5;
        return d3 * d3 + d4 * d4 + d5 * d5;
    }

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		
		if (this.oxygenBubble == null)
		{
			this.oxygenBubble = new GCCoreEntityOxygenBubble(this.worldObj, new Vector3(this), this);
			
			if (!this.worldObj.isRemote)
			{
				this.worldObj.spawnEntityInWorld(this.oxygenBubble);
			}
		}

		this.wattsReceived += ElectricItemHelper.dechargeItem(this.containingItems[0], GCCoreTileEntityOxygenDistributor.WATTS_PER_TICK, this.getVoltage());
		
		if (!this.worldObj.isRemote)
		{
			if (this.timeSinceOxygenRequest > 0)
			{
				GCCoreTileEntityOxygenDistributor.timeSinceOxygenRequest--;
			}
			
			this.wattsReceived = Math.max(this.wattsReceived - GCCoreTileEntityOxygenDistributor.WATTS_PER_TICK / 4, 0);
			
			if (this.power >= 1 && this.wattsReceived > 0)
			{
				this.active = true;
			}
			else
			{
				this.active = false;
		    	
		    	for (int x = (int) Math.floor(this.xCoord - this.power * 1.5); x < Math.ceil(this.xCoord + this.power * 1.5); x++)
		    	{
		        	for (int y = (int) Math.floor(this.yCoord - this.power * 1.5); y < Math.ceil(this.yCoord + this.power * 1.5); y++)
		        	{
		            	for (int z = (int) Math.floor(this.zCoord - this.power * 1.5); z < Math.ceil(this.zCoord + this.power * 1.5); z++)
		            	{
		            		TileEntity tile = this.worldObj.getBlockTileEntity(x, y, z);
		            		
		            		if (tile != null && tile instanceof GCCoreTileEntityBreathableAir)
		            		{
		        				tile.worldObj.setBlockToAir(tile.xCoord, tile.yCoord, tile.zCoord);
								tile.invalidate();
		            		}
		            		
		            		if (tile != null && tile instanceof GCCoreTileEntityUnlitTorch)
		            		{
		            			tile.worldObj.setBlock(tile.xCoord, tile.yCoord, tile.zCoord, GCCoreBlocks.unlitTorch.blockID, 0, 3);
		            		}
		            	}
		        	}
		    	}
			}
			
//			if (this.bubble == null && this.active)
//			{
//				this.bubble = new OxygenBubble(this);
//			}
//
//			if (this.bubble != null)
//			{
//				if (!this.bubble.connectedDistributors.contains(this))
//				{
//					this.bubble.connectedDistributors.add(this);
//				}
//				
//				if (this.lastPower != this.power)
//				{
//					this.bubble.calculate();
//				}
//			}

			if (this.power > 0)
			{
				this.power -= 1;
			}

			if (this.ticks % 3 == 0)
			{
				PacketManager.sendPacketToClients(this.getDescriptionPacket(), this.worldObj, new Vector3(this), 6);
			}
			
			this.lastPower = this.power;
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		Packet p = PacketManager.getPacket(BasicComponents.CHANNEL, this, this.power, this.wattsReceived, this.disabledTicks);
		return p;
	}

	@Override
	public void handlePacketData(INetworkManager network, int type, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try
		{
			if (this.worldObj.isRemote)
			{
				this.power = dataStream.readInt();
				this.wattsReceived = dataStream.readInt();
				this.disabledTicks = dataStream.readInt();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public ElectricityPack getRequest()
	{
		if (GCCoreTileEntityOxygenDistributor.timeSinceOxygenRequest > 0)
		{
			return new ElectricityPack(GCCoreTileEntityOxygenDistributor.WATTS_PER_TICK / this.getVoltage(), this.getVoltage());
		}
		else
		{
			return new ElectricityPack();
		}
	}

//	public void updateAdjacentOxygenAdd(int xOffset, int yOffset, int zOffset)
//	{
//		final TileEntity tile = this.worldObj.getBlockTileEntity(this.xCoord + xOffset, this.yCoord + yOffset, this.zCoord + zOffset);
//
//		if (tile != null && tile instanceof GCCoreTileEntityBreathableAir)
//		{
//			final GCCoreTileEntityBreathableAir air = (GCCoreTileEntityBreathableAir) tile;
//
//			air.addDistributor(this);
//		}
//	}
//
//	public void updateAdjacentOxygenRemove(int xOffset, int yOffset, int zOffset)
//	{
//		final TileEntity tile = this.worldObj.getBlockTileEntity(this.xCoord + xOffset, this.yCoord + yOffset, this.zCoord + zOffset);
//
//		if (tile != null && tile instanceof GCCoreTileEntityBreathableAir)
//		{
//			final GCCoreTileEntityBreathableAir air = (GCCoreTileEntityBreathableAir) tile;
//
//			air.removeDistributor(this);
//		}
//	}

	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);

        final NBTTagList var2 = par1NBTTagCompound.getTagList("Items");
        this.containingItems = new ItemStack[this.getSizeInventory()];

        for (int var3 = 0; var3 < var2.tagCount(); ++var3)
        {
            final NBTTagCompound var4 = (NBTTagCompound)var2.tagAt(var3);
            final byte var5 = var4.getByte("Slot");

            if (var5 >= 0 && var5 < this.containingItems.length)
            {
                this.containingItems[var5] = ItemStack.loadItemStackFromNBT(var4);
            }
        }
	}

	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeToNBT(par1NBTTagCompound);

        final NBTTagList list = new NBTTagList();

        for (int var3 = 0; var3 < this.containingItems.length; ++var3)
        {
            if (this.containingItems[var3] != null)
            {
                final NBTTagCompound var4 = new NBTTagCompound();
                var4.setByte("Slot", (byte)var3);
                this.containingItems[var3].writeToNBT(var4);
                list.appendTag(var4);
            }
        }

        par1NBTTagCompound.setTag("Items", list);
	}

	@Override
	public int transferGasToAcceptor(int amount, EnumGas type) 
	{
		this.timeSinceOxygenRequest = 20;
		
		if (this.wattsReceived > 0 && type == EnumGas.OXYGEN)
		{
			this.power = Math.max(this.power, amount);
			return 0;
		}
		else
		{
			return amount;
		}
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, EnumGas type) 
	{
		return side == ForgeDirection.getOrientation(this.getBlockMetadata() + 2).getOpposite();
	}

	@Override
	public boolean canTubeConnect(ForgeDirection direction) 
	{
		return direction == ForgeDirection.getOrientation(this.getBlockMetadata() + 2).getOpposite();
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return direction == ForgeDirection.getOrientation(this.getBlockMetadata() + 2);
	}

	@Override
	public int getSizeInventory()
	{
		return this.containingItems.length;
	}

	@Override
	public ItemStack getStackInSlot(int par1)
	{
		return this.containingItems[par1];
	}

	@Override
	public ItemStack decrStackSize(int par1, int par2)
	{
		if (this.containingItems[par1] != null)
		{
			ItemStack var3;

			if (this.containingItems[par1].stackSize <= par2)
			{
				var3 = this.containingItems[par1];
				this.containingItems[par1] = null;
				return var3;
			}
			else
			{
				var3 = this.containingItems[par1].splitStack(par2);

				if (this.containingItems[par1].stackSize == 0)
				{
					this.containingItems[par1] = null;
				}

				return var3;
			}
		}
		else
		{
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int par1)
	{
		if (this.containingItems[par1] != null)
		{
			ItemStack var2 = this.containingItems[par1];
			this.containingItems[par1] = null;
			return var2;
		}
		else
		{
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
	{
		this.containingItems[par1] = par2ItemStack;

		if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit())
		{
			par2ItemStack.stackSize = this.getInventoryStackLimit();
		}
	}

	@Override
	public String getInvName()
	{
		return "Oxygen Distributor";
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer)
	{
		return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : par1EntityPlayer.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void openChest()
	{
		if (!this.worldObj.isRemote)
		{
			PacketManager.sendPacketToClients(this.getDescriptionPacket(), this.worldObj, new Vector3(this), 15);
		}
		
		this.playersUsing++;
	}

	@Override
	public void closeChest()
	{
		this.playersUsing--;
	}
	
	// ISidedInventory Implementation:

	@Override
	public int[] getSizeInventorySide(int side) 
	{
		return new int[] {0};
	}

	@Override
	public boolean func_102007_a(int slotID, ItemStack itemstack, int side) 
	{
		return isStackValidForSlot(slotID, itemstack);
	}

	@Override
	public boolean func_102008_b(int slotID, ItemStack itemstack, int side) 
	{
		return slotID == 0;
	}

	@Override
	public boolean isInvNameLocalized() 
	{
		return true;
	}

	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemstack) 
	{
		return slotID == 0 ? itemstack.getItem() instanceof IItemElectric : false;
	}
}