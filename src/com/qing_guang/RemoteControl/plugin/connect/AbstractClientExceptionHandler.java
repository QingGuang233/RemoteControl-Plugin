package com.qing_guang.RemoteControl.plugin.connect;

import java.io.IOException;

import com.qing_guang.RemoteControl.util.ExceptionHandler;

/**
 * 抽象的客户端异常处理类
 * @author Qing_Guang
 *
 */
public abstract class AbstractClientExceptionHandler implements ExceptionHandler<IOException>{

	/**
	 * 客户端
	 */
	protected RemoteControlClient client;
	
	/**
	 * 设置此处理类对象对应的客户端
	 * @param client
	 */
	public final void setClient(RemoteControlClient client) {
		this.client = client;
	}
	
	/**
	 * 克隆一个处理类对象
	 * @see java.lang.Object#clone()
	 */
	public abstract AbstractClientExceptionHandler clone();
	
}
