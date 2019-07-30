package com.qing_guang.RemoteControl.plugin.setout;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

/**
 * 后台信息记录器
 * @author Qing_Guang
 *
 */
public final class Recorder {

	/**
	 * 默认的后台信息处理器
	 */
	public final static LogAppender APPENDER = new LogAppender();
	/**
	 * 本类的唯一实例化对象
	 */
	public final static Recorder INSTANCE = new Recorder();
	
	/**
	 * 调用此空方法以初始化 APPENDER 和 INSTANCE 字段
	 */
	public static void init() {}
	
	//构造方法不公开
	private Recorder() {
		
		if(INSTANCE != null) {
			throw new IllegalAccessError();
		}
		
		Logger logger = (Logger)LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
		logger.addAppender(APPENDER);
		
	}
	
	/**
	 * 获取所有在缓冲区里的后台信息
	 * @return 所有在缓冲区里的后台信息
	 */
	public List<String> getAll(){
		List<String> list = new ArrayList<>();
		synchronized (LogAppender.BUFFER) {
			for(int i = 0;i < LogAppender.BUFFER.size();i++) {
				list.add(LogAppender.BUFFER.get());
			}
			LogAppender.BUFFER.insertToStart();
		}
		return list;
	}
	
}
