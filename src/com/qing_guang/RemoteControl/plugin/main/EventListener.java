package com.qing_guang.RemoteControl.plugin.main;

import java.util.logging.Logger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.qing_guang.RemoteControl.packet.server.BackstageInfoPacket;
import com.qing_guang.RemoteControl.packet.server.OnlineModeChangePacket;
import com.qing_guang.RemoteControl.packet.server.OnlineModeChangePacket.Type;
import com.qing_guang.RemoteControl.plugin.connect.RemoteControlClient;
import com.qing_guang.RemoteControl.plugin.connect.RemoteControlServer.DisconnectionCause;
import com.qing_guang.RemoteControl.plugin.event.ClientDisconnectEvent;
import com.qing_guang.RemoteControl.plugin.event.ClientLoginSuccessEvent;
import com.qing_guang.RemoteControl.plugin.event.NewBackstageInfoEvent;

/**
 * 事件监听类
 * @author Qing_Guang
 *
 */
public class EventListener implements Listener{
	
	//默认的客户端成功登陆监听器
	@EventHandler
	public void success(ClientLoginSuccessEvent event) {
		
		Logger logger = JavaPlugin.getPlugin(Main.class).getLogger();
		logger.info("有一个客户端已创建连接并登陆");
		logger.info("用户名: " + event.getClient().getUname());
		
		for(RemoteControlClient client : Main.server.getClients()) {
			client.addPacketWillSend(new OnlineModeChangePacket(Type.CLIENT,true,event.getClient().getUname()), false);
		}
		
	}
	
	//默认的控制台新信息监听器
	//恭喜你发现了彩蛋23333
	@EventHandler
	public void ignb_new_bkstg_info(NewBackstageInfoEvent event) {
		for(RemoteControlClient client : Main.server.getClients()) {
			client.addPacketWillSend(new BackstageInfoPacket(event.getContent()), false);
		}
	}
	
	//默认的客户端断线监听器
	@EventHandler
	public void disconn(ClientDisconnectEvent event) {
		
		if(event.getCause() != DisconnectionCause.INPUT_ERROR && event.getCause() != DisconnectionCause.OUTPUT_ERROR && event.getCause() != DisconnectionCause.SERVER_CLOSE) {
			
			Logger logger = JavaPlugin.getPlugin(Main.class).getLogger();
			logger.info("有一个客户端已断开连接");
			logger.info("用户名: " + event.getUname());
			logger.info("断开原因: " + event.getCause());
			logger.info("是否成功: " + (event.isSuccess() ? "是" : "否"));
			
		}
		
		for(RemoteControlClient client : Main.server.getClients()) {
			client.addPacketWillSend(new OnlineModeChangePacket(Type.CLIENT,false,event.getUname()), false);
		}
		
	}
	
	//插件开启监听器
	@EventHandler
	public void onEnable(PluginEnableEvent event) {
		for(RemoteControlClient client : Main.server.getClients()) {
			client.addPacketWillSend(new OnlineModeChangePacket(Type.PLUGIN, true, event.getPlugin().getName()),false);
		}
	}
	
	//插件关闭监听器
	@EventHandler
	public void onDisable(PluginDisableEvent event) {
		for(RemoteControlClient client : Main.server.getClients()) {
			client.addPacketWillSend(new OnlineModeChangePacket(Type.PLUGIN, false, event.getPlugin().getName()),false);
		}
	}
	
	//玩家加入监听器
	@EventHandler
	public void join(PlayerJoinEvent event) {
		for(RemoteControlClient client : Main.server.getClients()) {
			client.addPacketWillSend(new OnlineModeChangePacket(Type.PLAYER,true,event.getPlayer().getName()), false);
		}
	}
	
	//玩家退出监听器
	@EventHandler
	public void quit(PlayerQuitEvent event) {
		for(RemoteControlClient client : Main.server.getClients()) {
			client.addPacketWillSend(new OnlineModeChangePacket(Type.CLIENT,false,event.getPlayer().getName()), false);
		}
	}

}
