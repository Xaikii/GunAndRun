package net.perpetualeve.gunandrun;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;

public interface GARPacket {

	void write(PacketBuffer buf);
	void read(PacketBuffer buf);
	void handlePacket(PlayerEntity player);
}