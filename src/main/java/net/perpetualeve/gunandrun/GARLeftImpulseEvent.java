package net.perpetualeve.gunandrun;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraftforge.eventbus.api.Event;

public class GARLeftImpulseEvent extends Event {

	float mult = 1.0f;
	ClientPlayerEntity player;
	
	public GARLeftImpulseEvent(float mult, ClientPlayerEntity player)
	{
		this.mult = mult;
		this.player = player;
	}
	
	public ClientPlayerEntity getPlayer() {
		return player;
	}
	
	public float getMult() {
		return mult;
	}

	public void setMult(float mult) {
		this.mult = mult;
	}
}
