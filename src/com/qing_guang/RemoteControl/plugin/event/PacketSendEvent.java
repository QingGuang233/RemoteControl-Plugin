package com.qing_guang.RemoteControl.plugin.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.qing_guang.RemoteControl.packet.Packet;

/**
 * 当一个数据包将要被添加到输出缓冲区时被调用
 * @author Qing_Guangy
 *
 */
public class PacketSendEvent extends Event implements Cancellable{

	private static final HandlerList handlers = new HandlerList();
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private Packet<?> pkt;
	private boolean encrypt;
	private boolean rsa_or_aes;
	private boolean isCancelled;

	/**
	 * 新建本事件对象
	 * @param pkt 将要被添加到输出缓冲区的数据包
	 * @param encrypt 是否加密
	 * @param rsa_or_aes 发送的加密算法,若为true则使用rsa算法加密,否则使用aes算法加密
	 */
	public PacketSendEvent(Packet<?> pkt, boolean encrypt, boolean rsa_or_aes) {
		super();
		this.pkt = pkt;
		this.encrypt = encrypt;
		this.rsa_or_aes = rsa_or_aes;
	}

	/**
	 * 将要被添加到输出缓冲区的数据包
	 */
	public Packet<?> getPkt() {
		return pkt;
	}

	/**
	 * 是否加密
	 */
	public boolean isEncrypt() {
		return encrypt;
	}

	/**
	 * 设置是否加密
	 */
	public void setEncrypt(boolean encrypt) {
		this.encrypt = encrypt;
	}

	/**
	 * 发送的加密算法,若为true则使用rsa算法加密,否则使用aes算法加密
	 */
	public boolean encryptAlg() {
		return rsa_or_aes;
	}

	/**
	 * 设置发送的加密算法,若为true则使用rsa算法加密,否则使用aes算法加密
	 */
	public void setEncryptAlg(boolean rsa_or_aes) {
		this.rsa_or_aes = rsa_or_aes;
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
