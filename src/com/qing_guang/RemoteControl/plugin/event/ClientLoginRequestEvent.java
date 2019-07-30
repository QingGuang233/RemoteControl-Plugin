package com.qing_guang.RemoteControl.plugin.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 当客户端发送登陆请求时被调用
 * @author Qing_Guang
 *
 */
public class ClientLoginRequestEvent extends Event implements Cancellable{

	private static final HandlerList handlers = new HandlerList();
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private String uname;
	private String pwd;
	private boolean isCancelled;
	
	/**
	 * 新建本事件对象
	 * @param uname 请求登陆的用户名
	 * @param pwd 请求登陆的密码(未经过md5加密)
	 */
	public ClientLoginRequestEvent(String uname,String pwd) {
		this.uname = uname;
		this.pwd = pwd;
	}
	
	/**
	 * 用户名
	 */
	public String getUname() {
		return uname;
	}
	
	/**
	 * 密码(未经过md5加密)
	 */
	public String getPwd() {
		return pwd;
	}

	/**
	 * @see org.bukkit.event.Cancellable#isCancelled()
	 */
	public boolean isCancelled() {
		return isCancelled;
	}

	/**
	 * @see org.bukkit.event.Cancellable#setCancelled(boolean)
	 */
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}
	
}
