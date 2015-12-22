package ganymedes01.headcrumbs.entity.vip;

import ganymedes01.headcrumbs.entity.EntityHuman;
import ganymedes01.headcrumbs.entity.VIPHandler;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class SnowShock35 extends VIPHandler {

	@Override
	protected ItemStack getItem(EntityHuman entity) {
		return new ItemStack(Items.snowball);
	}
}