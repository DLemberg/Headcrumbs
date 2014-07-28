package ganymedes01.headcrumbs.libs;

public class Reference {

	public static final String MOD_ID = "headcrumbs";
	public static final String MOD_NAME = "Headcrumbs";
	public static final String DEPENDENCIES = "required-after:Forge@[10.13.0.1180,);";
	public static final String VERSION_NUMBER = "1.0.0";

	public static final String ITEM_BLOCK_TEXTURE_PATH = MOD_ID + ":";
	public static final String ARMOUR_TEXTURE_PATH = ITEM_BLOCK_TEXTURE_PATH + "textures/models/armor/";
	public static final String ENTITY_TEXTURE_PATH = ITEM_BLOCK_TEXTURE_PATH + "textures/entities/";
	public static final String GUI_TEXTURE_PATH = ITEM_BLOCK_TEXTURE_PATH + "textures/gui/";

	public static final String CLIENT_PROXY_CLASS = "ganymedes01." + MOD_ID + ".proxy.ClientProxy";
	public static final String SERVER_PROXY_CLASS = "ganymedes01." + MOD_ID + ".proxy.CommonProxy";

	public static final String GUI_FACTORY_CLASS = "ganymedes01.headcrumbs.configs.ConfigGuiFactory";
}