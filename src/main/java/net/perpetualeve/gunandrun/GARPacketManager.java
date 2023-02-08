package net.perpetualeve.gunandrun;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class GARPacketManager {

	public static final GARPacketManager MANAGER = new GARPacketManager();
	
	public SimpleChannel channel;
	final String version = "1.0.0";
	
	public void init() {
		channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(GunAndRun.MODID), () -> version, version::equals, version::equals);
		registerPacket(1, GARConfigSyncPacket.class, GARConfigSyncPacket::new);
	}
	
	public <T extends GARPacket> void registerPacket(int index, Class<T> packet, Supplier<T> creator)
	{
		channel.registerMessage(index, packet, this::writePacket, K -> readPacket(K, creator), this::handlePacket);
	}
	
	protected void writePacket(GARPacket packet, PacketBuffer buffer)
	{
		try
		{
			packet.write(buffer);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	protected <T extends GARPacket> T readPacket(PacketBuffer buffer, Supplier<T> values)
	{
		try
		{
			T packet = values.get();
			packet.read(buffer);
			return packet;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	protected void handlePacket(GARPacket packet, Supplier<NetworkEvent.Context> provider)
	{
		try
		{
			Context context = provider.get();
			PlayerEntity player = getPlayer(context);
			context.enqueueWork(() -> packet.handlePacket(player));
			context.setPacketHandled(true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	protected PlayerEntity getPlayer(Context cont)
	{
		PlayerEntity entity = cont.getSender();
		return entity != null ? entity : getClientPlayer();
	}
	
	@OnlyIn(Dist.CLIENT)
	protected PlayerEntity getClientPlayer() {
		return Minecraft.getInstance().player;
	}
	
	public void sendToServer(GARPacket packet)
	{
		channel.send(PacketDistributor.SERVER.noArg(), packet);
	}
	
	public void sendToPlayer(GARPacket packet, PlayerEntity player)
	{
		if(!(player instanceof ServerPlayerEntity))
		{
			throw new RuntimeException("Sending a Packet to a Player from client is not allowed");
		}
		channel.send(PacketDistributor.PLAYER.with(() -> ((ServerPlayerEntity)player)), packet);
	}
	
	public void sendToAllPlayers(GARPacket packet)
	{
		channel.send(PacketDistributor.ALL.noArg(), packet);
	}
	
	public void sendToAllDim(World level, GARPacket packet)
	{
		channel.send(PacketDistributor.DIMENSION.with(() -> level.dimension()), packet);
	}
	
	public void sendToAllChunkWatchers(Chunk chunk, GARPacket packet)
	{
		channel.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), packet);
	}
	
	public void sendToAllEntityWatchers(Entity entity, GARPacket packet)
	{
		channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
	}
	
	public void sendToNearby(World level, BlockPos pos, int radius, GARPacket packet)
	{
		sendToNearby(new TargetPoint(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5, radius, level.dimension()), packet);
	}
	
	public void sendToNearby(TargetPoint point, GARPacket packet)
	{
		channel.send(PacketDistributor.NEAR.with(() -> point), packet);
	}
}