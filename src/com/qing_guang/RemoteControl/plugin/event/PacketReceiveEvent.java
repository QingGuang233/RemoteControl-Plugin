package com.qing_guang.RemoteControl.plugin.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.qing_guang.RemoteControl.packet.Packet;
import com.qing_guang.RemoteControl.plugin.connect.RemoteControlClient;

/**
 * 当一个客户端接收到数据包时被调用
 * @author Qing_Guang
 *
 */
public class PacketReceiveEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private Packet<?> pkt;
	private RemoteControlClient client;

	/**
	 * 新建本事件对象
	 * @param pkt 接收到的数据包
	 * @param client 接收到数据包的客户端
	 */
	public PacketReceiveEvent(Packet<?> pkt, RemoteControlClient client) {
		super();
		this.pkt = pkt;
		this.client = client;
	}

	/**
	 * 接收到的数据包
	 */
	public Packet<?> getPkt() {
		return pkt;
	}

	/**
	 * 接收到数据包的客户端
	 */
	public RemoteControlClient getClient() {
		return client;
	}
	
}
