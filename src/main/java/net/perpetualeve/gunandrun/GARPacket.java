package net.perpetualeve.gunandrun;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public interface GARPacket {

	void write(FriendlyByteBuf buf);
	void read(FriendlyByteBuf buf);
	void handlePacket(Player player);
}
