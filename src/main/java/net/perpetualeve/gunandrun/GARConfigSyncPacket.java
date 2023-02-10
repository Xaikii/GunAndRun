package net.perpetualeve.gunandrun;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.perpetualeve.gunandrun.GunAndRun.Entries;

public class GARConfigSyncPacket implements GARPacket {

	float forward;
	float side;
	Map<Item, Entries> overrides;
	
	public GARConfigSyncPacket(float forward, float side, Map<Item, Entries> overrides) {
		super();
		this.forward = forward;
		this.side = side;
		this.overrides = new HashMap<>(overrides);
	}
	public GARConfigSyncPacket() {
		
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeFloat(forward);
		buf.writeFloat(side);
		buf.writeMap(overrides, (T,V) -> T.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, V), (T,V) -> V.write(T));
	}

	@Override
	public void read(FriendlyByteBuf buf) {
		forward = buf.readFloat();
		side = buf.readFloat();
		overrides = buf.readMap(T -> T.readRegistryIdUnsafe(ForgeRegistries.ITEMS), Entries::new);
	}

	@Override
	public void handlePacket(Player player) {
		GunAndRun.forward = forward;
		GunAndRun.left = side;
		GunAndRun.overrides = overrides;
	}

}
