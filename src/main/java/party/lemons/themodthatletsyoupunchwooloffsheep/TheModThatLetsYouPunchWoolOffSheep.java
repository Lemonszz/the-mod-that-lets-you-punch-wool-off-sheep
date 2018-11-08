package party.lemons.themodthatletsyoupunchwooloffsheep;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

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
    public static final String VERSION = "1.1.0";
    public static final String NAME = "The Mod That Let's You Punch Wool Off Sheep";

    @SubscribeEvent
    public static void onSheepPunch(LivingAttackEvent event)
    {
        if(event.getEntity().world.isRemote)
            return;

        EntityLivingBase attacked = event.getEntityLiving();
        if(event.getAmount() < 1F || attacked.isDead || attacked.deathTime > 0 || attacked.hurtTime > 0)
            return;

        if(attacked instanceof IShearable)
        {
            shearShearable(attacked, event.getSource().getTrueSource());
        }
        else if(ModConfig.BIRDS_SHEAD_FEATHERS && (attacked instanceof EntityChicken || attacked instanceof EntityParrot))
        {
            shearBird(attacked);
        }
        else if(ModConfig.SLIMES_DROP_SLIME && attacked instanceof EntitySlime)
        {
            gloopSlime(attacked);
        }
    }

    public static void gloopSlime(EntityLivingBase entity)
    {
        if(entity.world.rand.nextInt(100) <= ModConfig.SLIME_DROP_CHANCE)
        {
            Item drop = entity instanceof EntityMagmaCube ? Items.MAGMA_CREAM : Items.SLIME_BALL;
            ItemStack droppedStack = new ItemStack(drop);

            dropItemAtEntity(entity, entity.getRNG(), droppedStack);
        }
    }

    public static void shearBird(EntityLivingBase entity)
    {
        if(entity.world.rand.nextInt(100) <= ModConfig.BIRD_FEATHER_DROP_CHANCE)
        {
            dropItemAtEntity(entity, entity.getRNG(), new ItemStack(Items.FEATHER));
        }
    }

    public static void shearShearable(EntityLivingBase entity, Entity attacker)
    {
        boolean shear = true;
        ItemStack shearItem = ItemStack.EMPTY;

        if(!ModConfig.WOLVES_SHEAR_SHEEP)
        {
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
            IShearable shearable = (IShearable) entity;

            BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
            if (shearable.isShearable(shearItem, entity.world, pos))
            {
                java.util.List<ItemStack> drops = shearable.onSheared(shearItem, entity.world, pos, ModConfig.WOOL_AMOUNT_MODIFIER);
                for(ItemStack stack : drops)
                {
                    dropItemAtEntity(entity, entity.getRNG(), stack);
                }
            }
        }
    }

    public static void dropItemAtEntity(Entity entity, Random rand, ItemStack stack)
    {
        net.minecraft.entity.item.EntityItem ent = entity.entityDropItem(stack, 1.0F);
        ent.motionY += rand.nextFloat() * 0.05F;
        ent.motionX += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
        ent.motionZ += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
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
        public static boolean BIRDS_SHEAD_FEATHERS = true;
        public static boolean SLIMES_DROP_SLIME = true;
        public static float SLIME_DROP_CHANCE = 25F;
        public static int BIRD_FEATHER_DROP_CHANCE = 50;
        public static int WOOL_AMOUNT_MODIFIER = -1;
    }
}
