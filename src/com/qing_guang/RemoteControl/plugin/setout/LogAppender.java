package com.qing_guang.RemoteControl.plugin.setout;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.qing_guang.RemoteControl.lib.main.Main;
import com.qing_guang.RemoteControl.plugin.event.NewBackstageInfoEvent;

/**
 * org.apache.logging.log4j.LogManager.ROOT_LOGGER_NAME的一个信息处理器,用来处理控制台输出的所有信息
 * @author Qing_Guang
 *
 */
public class LogAppender extends AbstractAppender{
	
	static final CollectionBuffer<String> BUFFER = new CollectionBuffer<>(50);
	
	/**
	 * 新建一个信息处理器
	 */
	public LogAppender() {
		
		super("com.qing_guang.RemoteControl.plugin.main.Main", null, null);
		
		try {
			BufferedReader br = new BufferedReader(new FileReader("logs/latest.log"));
			String line = null;
			while((line = br.readLine()) != null) {
				BUFFER.add(line);
			}
			br.close();
		} catch (IOException e) {
			Logger logger = JavaPlugin.getPlugin(Main.class).getLogger();
			logger.info("在读取日志文件时出现问题!请把下面的报错信息反馈给作者");
			e.printStackTrace();
		}
		
		start();
		
	}

	/**
	 * 输出格式: [时:分:秒][线程名/信息等级]: 信息内容
	 * @see org.apache.logging.log4j.core.Appender.append(LogEvent)
	 */
	public void append(LogEvent event) {
		
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(event.getTimeMillis()));
		
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		
		StringBuilder sb = new StringBuilder();
		
		String str = sb.append("[" + hour + ":" + minute + ":" + second + "] ")
					   .append("[" + event.getThreadName() + "/" + event.getLevel().toString() + "]: ")
					   .append(event.getMessage().getFormattedMessage()).toString();
		
		BUFFER.add(str);
		
		Bukkit.getScheduler().callSyncMethod(JavaPlugin.getPlugin(Main.class), () -> {
			Bukkit.getPluginManager().callEvent(new NewBackstageInfoEvent(str));
			return null;
		});
		
	}
	
}
