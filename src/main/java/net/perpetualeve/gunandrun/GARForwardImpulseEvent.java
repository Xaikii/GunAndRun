package net.perpetualeve.gunandrun;

import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.eventbus.api.Event;

public class GARForwardImpulseEvent extends Event {

	float mult = 1.0f;
	LocalPlayer player;
	
	public GARForwardImpulseEvent(float mult,LocalPlayer player)
	{
		this.mult = mult;
		this.player = player;
	}
	
	public LocalPlayer getPlayer() {
		return player;
	}
	
	public float getMult() {
		return mult;
	}

	public void setMult(float mult) {
		this.mult = mult;
	}
}
