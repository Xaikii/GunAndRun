package net.perpetualeve.gunandrun;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
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
	public void write(PacketBuffer buf) {
		buf.writeFloat(forward);
		buf.writeFloat(side);
		buf.writeVarInt(overrides.size());
		for(Entry<Item, Entries> item : overrides.entrySet()) {
			buf.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, item.getKey());
			item.getValue().write(buf);
		}
	}

	@Override
	public void read(PacketBuffer buf) {
		forward = buf.readFloat();
		side = buf.readFloat();
		overrides = new HashMap<>();
		int size = buf.readVarInt();
		for(int i = 0; i<size;i++) {
			overrides.put(buf.readRegistryIdUnsafe(ForgeRegistries.ITEMS), new Entries(buf));
		}
	}

	@Override
	public void handlePacket(PlayerEntity player) {
		GunAndRun.forward = forward;
		GunAndRun.left = side;
		GunAndRun.overrides = overrides;
	}

}