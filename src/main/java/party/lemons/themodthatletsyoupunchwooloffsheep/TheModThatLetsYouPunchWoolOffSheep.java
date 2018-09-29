package party.lemons.themodthatletsyoupunchwooloffsheep;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by Sam on 29/09/2018.
 */
@Mod(modid =
        TheModThatLetsYouPunchWoolOffSheep.MODID,
        name=TheModThatLetsYouPunchWoolOffSheep.NAME,
        version = TheModThatLetsYouPunchWoolOffSheep.VERSION,
        acceptableRemoteVersions = "*"
)
@Mod.EventBusSubscriber(modid = TheModThatLetsYouPunchWoolOffSheep.MODID)
public class TheModThatLetsYouPunchWoolOffSheep
{
    public static final String MODID = "themodthatletsyoupunchwooloffsheep";
    public static final String VERSION = "1.0.0";
    public static final String NAME = "The Mod That Let's You Punch Wool Off Sheep";

    @SubscribeEvent
    public static void onSheepPunch(LivingAttackEvent event)
    {
        if(event.getEntity().world.isRemote)
            return;

        EntityLivingBase attacked = event.getEntityLiving();
        if(attacked instanceof IShearable)
        {
            boolean shear = true;
            ItemStack shearItem = ItemStack.EMPTY;

            if(!ModConfig.WOLVES_SHEAR_SHEEP)
            {
                Entity attacker = event.getSource().getTrueSource();
                if(attacker != null && attacker instanceof EntityPlayer)
                {
                    shearItem = ((EntityPlayer) attacker).getHeldItemMainhand();
                }
                else
                {
                    shear = false;
                }
            }

            if(shear)
            {
                IShearable shearable = (IShearable) attacked;

                BlockPos pos = new BlockPos(attacked.posX, attacked.posY, attacked.posZ);
                if (shearable.isShearable(shearItem, attacked.world, pos))
                {
                    java.util.List<ItemStack> drops = shearable.onSheared(shearItem, attacked.world, pos, ModConfig.WOOL_AMOUNT_MODIFIER);
                    java.util.Random rand = new java.util.Random();
                    for(ItemStack stack : drops)
                    {
                        net.minecraft.entity.item.EntityItem ent = attacked.entityDropItem(stack, 1.0F);
                        ent.motionY += rand.nextFloat() * 0.05F;
                        ent.motionX += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
                        ent.motionZ += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onConfigReload(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if(event.getModID().equals(MODID))
        {
            ConfigManager.sync(MODID, Config.Type.INSTANCE);
        }
    }

    @Config(modid = MODID)
    public static class ModConfig
    {
        public static boolean WOLVES_SHEAR_SHEEP = true;
        public static int WOOL_AMOUNT_MODIFIER = -1;
    }
}
