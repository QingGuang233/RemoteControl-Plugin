package com.qing_guang.RemoteControl.plugin.connect;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.qing_guang.RemoteControl.lib.main.Main;
import com.qing_guang.RemoteControl.packet.server.ManualDisconnectedPacket;
import com.qing_guang.RemoteControl.packet.server.ServerClosePacket;
import com.qing_guang.RemoteControl.plugin.event.ClientDisconnectEvent;
import com.qing_guang.RemoteControl.util.ExceptionHandler;

/**
 * 默认的服务器实现类
 * @author Qing_Guang
 *
 */
public class RemoteControlServer extends Thread{

	private int port;
	private int max_client;
	private boolean isRunning;
	private ServerSocket server;
	private RemoteControlClient client_temp;
	private ExceptionHandler<IOException> server_cant_init;
	private ExceptionHandler<IOException> server_cant_stop;
	private AbstractClientExceptionHandler client_write_exc;
	private AbstractClientExceptionHandler client_read_exc;
	private AbstractClientExceptionHandler client_close_exc;
	Map<String,RemoteControlClient> logged;
	
	/**
	 * 创建一个默认的服务器
	 * @param port 使用的端口
	 * @param max_client 最大允许的客户端数量
	 * @param client_temp 客户端的模板
	 * @param server_cant_init 服务器无法开启的异常处理器
	 * @param server_cant_stop 服务器无法关闭的异常处理器
	 * @param client_write_exc 客户端输出数据异常处理器(模板)
	 * @param client_read_exc 客户端接收数据异常处理器(模板)
	 * @param client_close_exc 无法关闭客户端的异常处理器(模板)
	 */
	public RemoteControlServer(int port,int max_client,RemoteControlClient client_temp
			,ExceptionHandler<IOException> server_cant_init,ExceptionHandler<IOException> server_cant_stop
			,AbstractClientExceptionHandler client_write_exc,AbstractClientExceptionHandler client_read_exc
			,AbstractClientExceptionHandler client_close_exc) {
		this.port = port;
		this.max_client = max_client;
		this.client_temp = client_temp;
		this.server_cant_init = server_cant_init;
		this.server_cant_stop = server_cant_stop;
		this.client_write_exc = client_write_exc;
		this.client_read_exc = client_read_exc;
		this.client_close_exc = client_close_exc;
		logged = new LinkedHashMap<>();
	}
	
	/**
	 * 开始运行服务器
	 */
	public void run(){
		
		try {
			
			server = new ServerSocket(port);
			isRunning = true;
			
		}catch(IOException e) {
			server_cant_init.handle(e);
		}
			
		while(isRunning()) {
			
			try {
			
				if(logged.size() >= max_client) {
					continue;
				}
				
				Socket c = server.accept();
				RemoteControlClient client = client_temp.clone();
				client.init(c, client_write_exc.clone(), client_read_exc.clone(), this);
				client.start();
			
			}catch (IOException e){
			}
			
		}
		
	}
	
	/**
	 * 服务器是否运行
	 */
	public boolean isRunning() {
		return isRunning;
	}
	
	/**
	 * 关闭服务器
	 */
	public void disable() {
		
		isRunning = false;
		
		try {
			
			new Socket("localhost",port).close();
			server.close();
			
			for(String uname : logged.keySet()) {
				RemoteControlClient client = logged.get(uname);
				client.addPacketWillSend(new ServerClosePacket(),false);
				disconn(client,DisconnectionCause.SERVER_CLOSE);
			}
			
			logged.clear();
			
		} catch (IOException e) {
			server_cant_stop.handle(e);
		}
	}
	
	/**
	 * 断线一个客户端
	 * @param uname 客户端的用户名
	 * @param cause 断线原因
	 * @throws IllegalArgumentException 当此用户名没有被登陆时抛出
	 * @see #disconn(RemoteControlClient,DisconnectionCause)
	 */
	public void disconn(String uname,DisconnectionCause cause) throws IllegalArgumentException{
		if(getClient(uname) == null) {
			throw new IllegalArgumentException("Client not online");
		}
		disconn(getClient(uname),cause);
	}
	
	/**
	 * 断线一个客户端
	 * @param client 客户端
	 * @param cause 断线原因
	 */
	public void disconn(RemoteControlClient client,DisconnectionCause cause) {
		
		new Thread(() -> {
			
			boolean success = false;
			
			if(cause == DisconnectionCause.MANUAL) {
				try {
					client.addPacketWillSend(new ManualDisconnectedPacket(),false);
					Thread.sleep(100);
					waitForSendOver(client);
					client.disconn();
					success = true;
				} catch (IOException exc) {
					AbstractClientExceptionHandler client_close_exc = this.client_close_exc.clone();
					client_close_exc.setClient(client);
					client_close_exc.handle(exc);
					success = false;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else if(cause == DisconnectionCause.CLIENT_EXIT){
				try {
					client.disconn();
					success = true;
				}catch(IOException exc) {
					AbstractClientExceptionHandler client_close_exc = this.client_close_exc.clone();
					client_close_exc.setClient(client);
					client_close_exc.handle(exc);
					success = false;
				}
			}else if(cause == DisconnectionCause.SERVER_CLOSE){
				try {
					client.addPacketWillSend(new ServerClosePacket(),false);
					Thread.sleep(100);
					waitForSendOver(client);
					client.disconn();
					success = true;
				}catch(IOException exc) {
					success = false;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else if(cause == DisconnectionCause.INPUT_ERROR || cause == DisconnectionCause.OUTPUT_ERROR) {
				try {
					client.disconn();
					success = true;
				}catch(IOException exc) {
				}
			}
			
			logged.remove(client.getUname());
			
			boolean suc = success;
			Bukkit.getScheduler().callSyncMethod(JavaPlugin.getPlugin(Main.class), () -> {
				Bukkit.getPluginManager().callEvent(new ClientDisconnectEvent(client.getUname(),cause,suc));
				return null;
			});
			
		}).start();
		
	}
	
	/**
	 * 客户端是否在线
	 * @param uname 客户端的用户名
	 */
	public boolean isOnline(String uname) {
		return logged.containsKey(uname);
	}
	
	/**
	 * 获取所有的客户端
	 */
	public Collection<RemoteControlClient> getClients(){
		return logged.values();
	}
	
	/**
	 * 获取客户端
	 * @param uname 客户端的用户名
	 */
	public RemoteControlClient getClient(String uname) {
		return logged.get(uname);
	}
	
	/**
	 * 设置客户端的模板
	 */
	public void setClientTemp(RemoteControlClient client_temp) {
		this.client_temp = client_temp;
	}
	
	/**
	 * 设置服务器无法关闭的异常处理器
	 */
	public void setServerCantStopExcHandler(ExceptionHandler<IOException> server_cant_stop) {
		this.server_cant_stop = server_cant_stop;
	}
	
	/**
	 * 设置客户端输出数据异常处理器(模板)
	 */
	public void setClientWriteExcHandlerTemp(AbstractClientExceptionHandler client_write_exc) {
		this.client_write_exc = client_write_exc;
	}
	
	/**
	 * 客户端接收数据异常处理器(模板)
	 */
	public void setClientReadExcHandlerTemp(AbstractClientExceptionHandler client_read_exc) {
		this.client_read_exc = client_read_exc;
	}
	
	/**
	 * 无法关闭客户端的异常处理器(模板)
	 */
	public void setClientCloseExcHandlerTemp(AbstractClientExceptionHandler client_close_exc) {
		this.client_close_exc = client_close_exc;
	}
	
	private void waitForSendOver(RemoteControlClient client) {
		while(!client.noSendTask()) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 断线原因
	 * @author Qing_Guang
	 *
	 */
	public enum DisconnectionCause{
		
		/**
		 * 手动
		 */
		MANUAL,
		
		/**
		 * 客户端退出
		 */
		CLIENT_EXIT,
		
		/**
		 * 服务器关闭
		 */
		SERVER_CLOSE,
		
		/**
		 * 输入异常
		 */
		INPUT_ERROR,
		
		/**
		 * 输出异常
		 */
		OUTPUT_ERROR
	}
	
}
