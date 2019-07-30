package com.qing_guang.RemoteControl.plugin.connect;

import java.io.IOException;
import java.util.logging.Logger;

import com.qing_guang.RemoteControl.util.ExceptionHandler;

/**
 * 默认的服务器无法启动的异常处理器
 * @author Qing_Guang
 *
 */
public class ServerCannotInitExceptionHandler implements ExceptionHandler<IOException>{
	
	private Logger logger;
	
	/**
	 * 传入一个logger以新建一个异常处理类对象
	 */
	public ServerCannotInitExceptionHandler(Logger logger) {
		this.logger = logger;
	}

	/**
	 * @see com.qing_guang.RemoteControl.util.ExceptionHandler#handle()
	 */
	public void handle(IOException exc) {
		
		logger.warning("服务器因为端口被占用或其他的原因无法开启,请确认后再输入指令重新开启");
		logger.warning("下面是错误日志");
		exc.printStackTrace();
		
	}

}
