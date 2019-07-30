package com.qing_guang.RemoteControl.plugin.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.qing_guang.RemoteControl.plugin.connect.RemoteControlServer.DisconnectionCause;

/**
 * 当客户端被断线时被调用
 * @author Qing_Guang
 *
 */
public class ClientDisconnectEvent extends Event{

	private static final HandlerList handlers = new HandlerList();
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private String uname;
	private DisconnectionCause cause;
	private boolean success;
	
	/**
	 * 新建本事件对象
	 * @param uname 被断线的客户端的用户名
	 * @param cause 断线原因
	 * @param success 断线是否成功
	 */
	public ClientDisconnectEvent(String uname,DisconnectionCause cause,boolean success) {
		this.uname = uname;
		this.cause = cause;
		this.success = success;
	}
	
	/**
	 * 用户名
	 */
	public String getUname() {
		return uname;
	}
	
	/**
	 * 断线原因
	 */
	public DisconnectionCause getCause() {
		return cause;
	}
	
	/**
	 * 是否成功
	 */
	public boolean isSuccess() {
		return success;
	}
	
}
