package net.perpetualeve.gunandrun.mixins;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.MovementInput;
import net.minecraftforge.common.MinecraftForge;
import net.perpetualeve.gunandrun.GARForwardImpulseEvent;
import net.perpetualeve.gunandrun.GARLeftImpulseEvent;
import net.perpetualeve.gunandrun.GunAndRun;

@Mixin(ClientPlayerEntity.class)
public class GARLocalPlayerMixin {
	
	ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;
	
	@Redirect(method = "aiStep", at = @At(value = "FIELD", target = "Lnet/minecraft/util/MovementInput;forwardImpulse:F", opcode = Opcodes.PUTFIELD), remap = true)
	private void GAR_overwriteForwardImpulse(MovementInput input, float a) {
		GARForwardImpulseEvent event = new GARForwardImpulseEvent(GunAndRun.forward, player);
		MinecraftForge.EVENT_BUS.post(event);
		input.forwardImpulse *= event.getMult();
	}
	
	@Redirect(method = "aiStep", at = @At(value = "FIELD", target = "Lnet/minecraft/util/MovementInput;leftImpulse:F", opcode = Opcodes.PUTFIELD), remap = true)
	private void GAR_overwriteLeftImpulse(MovementInput input, float a) {
		GARLeftImpulseEvent event = new GARLeftImpulseEvent(GunAndRun.left, player);
		MinecraftForge.EVENT_BUS.post(event);
		input.leftImpulse *= event.getMult();
	}
}
