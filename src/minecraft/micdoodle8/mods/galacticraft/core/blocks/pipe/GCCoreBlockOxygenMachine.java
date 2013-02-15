//package micdoodle8.mods.galacticraft.core.blocks.pipe;
//
//import net.minecraft.block.Block;
//import net.minecraft.block.material.Material;
//import net.minecraft.entity.EntityLiving;
//import net.minecraft.util.MathHelper;
//import net.minecraft.world.World;
//
//public class GCCoreBlockOxygenMachine extends Block
//{
//	public GCCoreBlockOxygenMachine(int par1, int par2, Material par3Material) 
//	{
//		super(par1, par2, par3Material);
//	}
//	
//	@Override
//    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLiving par5EntityLiving)
//    {
//        GCCoreTileEntity tentity = (GCCoreTileEntity)par1World.getBlockTileEntity(par2, par3, par4);
//        
//        if (tentity != null)
//        {
//        	int yaw = MathHelper.floor_double((double)((par5EntityLiving.rotationYaw * 4F) / 360F) + 0.5D) & 3;
//            int pitch = Math.round(par5EntityLiving.rotationPitch);
//
//            if (pitch >= 65)
//            {
//            	tentity.setDirection(1);
//            }
//            else if (pitch <= -65)
//            {
//            	tentity.setDirection(0);
//            }
//            else
//            {
//                switch (yaw)
//                {
//                    case 0:
//                    	tentity.setDirection(2);
//                        break;
//
//                    case 1:
//                    	tentity.setDirection(5);
//                        break;
//
//                    case 2:
//                    	tentity.setDirection(3);
//                        break;
//
//                    case 3:
//                    	tentity.setDirection(4);
//                        break;
//                }
//            }
//        }
//    }
//}
