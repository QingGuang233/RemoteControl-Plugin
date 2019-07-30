package com.qing_guang.RemoteControl.plugin.connect;

import java.io.IOException;
import java.util.logging.Logger;

import com.qing_guang.RemoteControl.util.ExceptionHandler;

/**
 * 默认的服务器无法关闭的异常处理器
 * @author Qing_Guang
 *
 */
public class ServerCannotCloseExceptionHandler implements ExceptionHandler<IOException>{

	private Logger logger;
	
	/**
	 * 传入一个logger以新建一个异常处理类对象
	 */
	public ServerCannotCloseExceptionHandler(Logger logger) {
		this.logger = logger;
	}

	/**
	 * @see com.qing_guang.RemoteControl.util.ExceptionHandler#handle()
	 */
	public void handle(IOException exc) {
		
		logger.warning("服务器无法正常关闭!请把下面的错误日志发给作者!");
		exc.printStackTrace();
		
	}

}
