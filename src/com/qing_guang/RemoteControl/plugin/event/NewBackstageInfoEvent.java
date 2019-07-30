package com.qing_guang.RemoteControl.plugin.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 当控制台有新消息时被调用
 * @author Qing_Guang
 *
 */
public class NewBackstageInfoEvent extends Event{

	private static final HandlerList handlers = new HandlerList();
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private String content;
	
	/**
	 * 新建本事件对象
	 * @param content 新信息内容
	 */
	public NewBackstageInfoEvent(String content) {
		this.content = content;
	}
	
	/**
	 * 新信息内容
	 */
	public String getContent() {
		return content;
	}

}
