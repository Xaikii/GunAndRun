package net.perpetualeve.gunandrun;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod(GunAndRun.MODID)
public class GunAndRun {

	public static final String MODID = "gunandrun";
	
	DoubleValue defaultMultForward;
	DoubleValue defaultMultLeft;
	ConfigValue<List<? extends String>> overridesCFG;
	public static float forward;
	public static float left;
	public static Map<Item, Entries> overrides = new HashMap<>();

	public static ForgeConfigSpec CONFIG;
	
	public GunAndRun() {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		builder.push("general");
		builder.comment("The default multiplier for forward movement while using an item, 0.0d is no movement, 1.0d is normal movement");
		defaultMultForward = builder.defineInRange("forward_mult_default", 1.0d, 0.0d, 1.0d);
		builder.comment("The default multiplier for left movement while using an item, 0.0d is no movement, 1.0d is normal movement");
		defaultMultLeft = builder.defineInRange("side_mult_default", 1.0d, 0.0d, 1.0d);
		builder.comment("List of items to have other values, values are used against the default, e.g. forwardDefault is 0.7 and an item here has 0.5, result is 0.35 \"itemID;forwardMult;sideMult\" example: \"minecraft:crossbow;0.0;0.0\"");
		overridesCFG = builder.defineList("overrides", Collections.emptyList(), T -> true);
		builder.pop();
		CONFIG = builder.build();
		ModLoadingContext.get().registerConfig(Type.COMMON, CONFIG, "GunAndRun.toml");

		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::onLoad);
		bus.addListener(this::onFileChange);
		MinecraftForge.EVENT_BUS.register(this);
		GARPacketManager.MANAGER.init();
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.DEDICATED_SERVER)
	public void playerJoin(PlayerLoggedInEvent event) {
		GARPacketManager.MANAGER.sendToPlayer(new GARConfigSyncPacket(forward, left, overrides), event.getEntity());
	}
	
	public void onLoad(ModConfigEvent.Loading configEvent) {
		reloadConfig();
	}

	public void onFileChange(ModConfigEvent.Reloading configEvent) {
		reloadConfig();
	}
	
	public void reloadConfig() {
		if(!isAllowedToLoad()) return; 
		forward = defaultMultForward.get().floatValue();
		left = defaultMultLeft.get().floatValue();
		overrides.clear();
		for(String f:overridesCFG.get()) {
			String[] parts = f.split(";");
			overrides.put(ForgeRegistries.ITEMS.getValue(new ResourceLocation(parts[0])), new Entries(Float.parseFloat(parts[1]), Float.parseFloat(parts[2])));
		}
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server == null) return;
		GARPacketManager.MANAGER.sendToAllPlayers(new GARConfigSyncPacket(forward, left, overrides));
	}
	
	public boolean isAllowedToLoad() {
		if(FMLEnvironment.dist.isDedicatedServer()) {
			return true;
		}
		Minecraft mc = Minecraft.getInstance();
		if(mc.player == null) {
			return true;
		}
		return mc.isLocalServer();
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onForwardImpulse(GARForwardImpulseEvent event) {
		LocalPlayer player = event.getPlayer();
		
		Entries ent = overrides.get(player.getItemInHand(player.getUsedItemHand()).getItem());
		if(ent != null) {
			event.setMult(event.getMult() * ent.getForward());
		}
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onLeftImpulse(GARLeftImpulseEvent event) {
		LocalPlayer player = event.getPlayer();
		
		Entries ent = overrides.get(player.getItemInHand(player.getUsedItemHand()).getItem());
		if(ent != null) {
			event.setMult(event.getMult() * ent.getSide());
		}
	}
	
	
	public static class Entries {
		
		float forward;
		float side;
		
		Entries(float forward, float side) {
			this.forward = forward;
			this.side = side;
		}
		public Entries(FriendlyByteBuf buf) {
			this.forward = buf.readFloat();
			this.side = buf.readFloat();
		}

		public float getForward() {
			return forward;
		}

		public float getSide() {
			return side;
		}
		
		public void write(FriendlyByteBuf buf) {
			buf.writeFloat(forward);
			buf.writeFloat(side);
		}
	}
	
}
