package ganymedes01.headcrumbs.eventHandlers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ganymedes01.headcrumbs.Headcrumbs;
import ganymedes01.headcrumbs.ModItems;
import ganymedes01.headcrumbs.entity.EntityHuman;
import ganymedes01.headcrumbs.libs.SkullTypes;
import ganymedes01.headcrumbs.utils.HeadUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;

public class HandlerEvents {

	private static List<String> hardcodedBlacklist = Arrays.asList("Twilight Forest", "Erebus", "The Outer Lands");
	private static Item cleaver;

	@SubscribeEvent
	public void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
		if (event.entityLiving instanceof EntityHuman) {
			String name = event.world.provider.getDimensionName();
			if (hardcodedBlacklist.contains(name) || isDimensionBlackListed(event.world.provider.dimensionId))
				event.setResult(Result.DENY);
		}
	}

	private static boolean isDimensionBlackListed(int dimensionId) {
		for (int id : Headcrumbs.blacklistedDimensions)
			if (dimensionId == id)
				return true;
		return false;
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void playerDrop(LivingDeathEvent event) {
		EntityLivingBase entity = event.entityLiving;
		if (entity.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory") && entity instanceof EntityPlayerMP) {
			ArrayList<EntityItem> drops = new ArrayList<EntityItem>();

			ItemStack weapon = getWeapon(event.source);
			int looting = EnchantmentHelper.getEnchantmentLevel(Enchantment.looting.effectId, weapon);
			drop(event.entityLiving, event.source, looting, drops);

			if (!drops.isEmpty())
				for (EntityItem item : drops)
					((EntityPlayerMP) entity).joinEntityItemWithWorld(item);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void dropEvent(LivingDropsEvent event) {
		drop(event.entityLiving, event.source, event.lootingLevel, event.drops);
	}

	private void drop(EntityLivingBase entity, DamageSource source, int looting, List<EntityItem> drops) {
		if (entity.worldObj.isRemote)
			return;
		if (entity.getHealth() > 0.0F)
			return;

		boolean isPoweredCreeper = Headcrumbs.enableChargedCreeperKills && isPoweredCreeper(source);
		int beheading = getBeaheadingLevel(getWeapon(source));

		if (isPoweredCreeper || shouldDoRandomDrop(entity.worldObj.rand, beheading, looting)) {
			ItemStack stack = HeadUtils.getHeadfromEntity(entity);
			if (stack == null)
				return;

			if (beheading > 0 && stack.getItem() == Items.skull)
				return; // Vanilla head drops will be handled by TiCon

			if (stack.getItem() != Items.skull || Headcrumbs.enableVanillaHeadsDrop)
				if (isPlayerHead(stack) || Headcrumbs.enableMobsAndAnimalHeads)
					addDrop(stack, entity, drops);
		}
	}

	private boolean isPoweredCreeper(DamageSource source) {
		if (source.isExplosion() && source instanceof EntityDamageSource) {
			Entity entity = ((EntityDamageSource) source).getEntity();
			if (entity != null && entity instanceof EntityCreeper)
				return ((EntityCreeper) entity).getPowered();
		}

		return false;
	}

	private boolean isPlayerHead(ItemStack stack) {
		return stack.getItem() == ModItems.skull && stack.getItemDamage() == SkullTypes.player.ordinal();
	}

	private boolean shouldDoRandomDrop(Random rand, int beheading, int looting) {
		if (beheading > 0)
			return rand.nextInt(100) < beheading * 10;

		int chance = Math.max(1, Headcrumbs.headDropChance / Math.max(looting + 1, 1));
		return Headcrumbs.enableRandomHeadDrop && rand.nextInt(chance) == 0;
	}

	private int getBeaheadingLevel(ItemStack weapon) {
		if (Headcrumbs.isTinkersConstructLoaded) {
			if (cleaver == null)
				try {
					Class<?> TinkerTools = Class.forName("tconstruct.tools.TinkerTools");
					Field field = TinkerTools.getDeclaredField("cleaver");
					field.setAccessible(true);
					cleaver = (Item) field.get(null);
				} catch (Exception e) {
				}

			if (weapon == null || !weapon.hasTagCompound())
				return 0;

			if (weapon.getTagCompound().hasKey("InfiTool", Constants.NBT.TAG_COMPOUND)) {
				NBTTagCompound infiTool = weapon.getTagCompound().getCompoundTag("InfiTool");
				if (infiTool.hasKey("Beheading", Constants.NBT.TAG_INT)) {
					int beheading = infiTool.getInteger("Beheading");
					if (cleaver == weapon.getItem())
						beheading += 2;
					return beheading;
				}
			}
		}

		return 0;
	}

	private ItemStack getWeapon(DamageSource source) {
		if (source instanceof EntityDamageSource) {
			Entity entity = ((EntityDamageSource) source).getEntity();
			if (entity instanceof EntityPlayer)
				return ((EntityPlayer) entity).getCurrentEquippedItem();
		}
		return null;
	}

	private void addDrop(ItemStack stack, EntityLivingBase entity, List<EntityItem> drops) {
		if (stack.stackSize <= 0)
			return;
		List<EntityItem> toRemove = new ArrayList<EntityItem>();
		for (EntityItem drop : drops) {
			ItemStack dropStack = drop.getEntityItem();
			if (dropStack.getItem() == Items.skull && dropStack.getItemDamage() != 1) // Remove any head that isn't the wither skeleton's head
				toRemove.add(drop);
		}
		drops.removeAll(toRemove);

		EntityItem entityItem = new EntityItem(entity.worldObj, entity.posX, entity.posY, entity.posZ, stack);
		entityItem.delayBeforeCanPickup = 10;
		drops.add(entityItem);
	}
}