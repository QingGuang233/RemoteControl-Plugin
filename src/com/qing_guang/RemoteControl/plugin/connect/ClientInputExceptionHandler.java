package com.qing_guang.RemoteControl.plugin.connect;

import java.io.IOException;
import java.util.logging.Logger;

import com.qing_guang.RemoteControl.plugin.connect.RemoteControlServer.DisconnectionCause;

/**
 * 默认的客户端数据接收异常
 * @author Qing_Guang
 *
 */
public class ClientInputExceptionHandler extends AbstractClientExceptionHandler{
	
	private Logger logger;
	
	/**
	 * 传入一个logger以新建一个异常处理类对象
	 */
	public ClientInputExceptionHandler(Logger logger) {
		this.logger = logger;
	}

	/**
	 * @see com.qing_guang.RemoteControl.util.ExceptionHandler#handle()
	 */
	public void handle(IOException exc) {
		if(client != null && client.isRunning()) {
			client.getServer().disconn(client, DisconnectionCause.INPUT_ERROR);
			logger.warning("有一个客户端因数据输入异常已断开连接");
			logger.warning("用户名: " + client.getUname());
			logger.warning("下面是报错信息:");
			exc.printStackTrace();
		}
	}

	/**
	 * @see AbstractClientExceptionHandler#clone()
	 */
	public AbstractClientExceptionHandler clone() {
		return new ClientInputExceptionHandler(logger);
	}
	
}