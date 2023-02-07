package net.perpetualeve.gunandrun.mixins;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.perpetualeve.gunandrun.GARForwardImpulseEvent;
import net.perpetualeve.gunandrun.GARLeftImpulseEvent;
import net.perpetualeve.gunandrun.GunAndRun;

@Mixin(LocalPlayer.class)
public class GARLocalPlayerMixin {
	
	LocalPlayer player = (LocalPlayer)(Object)this;
	
	@Redirect(method = "aiStep", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/Input;forwardImpulse:F", opcode = Opcodes.PUTFIELD), remap = true)
	private void GAR_overwriteForwardImpulse(Input input, float a) {
		GARForwardImpulseEvent event = new GARForwardImpulseEvent(GunAndRun.forward, ((LocalPlayer)(Object)this));
		MinecraftForge.EVENT_BUS.post(event);
		input.forwardImpulse *= event.getMult();
	}
	
	@Redirect(method = "aiStep", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/Input;leftImpulse:F", opcode = Opcodes.PUTFIELD), remap = true)
	private void GAR_overwriteLeftImpulse(Input input, float a) {
		GARLeftImpulseEvent event = new GARLeftImpulseEvent(GunAndRun.left, ((LocalPlayer)(Object)this));
		MinecraftForge.EVENT_BUS.post(event);
		input.leftImpulse *= event.getMult();
	}
}
