package com.qing_guang.RemoteControl.plugin.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.qing_guang.RemoteControl.plugin.connect.RemoteControlClient;

/**
 * 当一个客户端成功登陆时被调用
 * @author Qing_Guang
 *
 */
public class ClientLoginSuccessEvent extends Event{
	
	private static final HandlerList handlers = new HandlerList();
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private RemoteControlClient client;
	
	/**
	 * 新建本事件对象
	 * @param client 成功登陆的客户端
	 */
	public ClientLoginSuccessEvent(RemoteControlClient client) {
		this.client = client;
	}
	
	/**
	 * 成功登陆的客户端
	 */
	public RemoteControlClient getClient() {
		return client;
	}
	
}
