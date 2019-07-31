package com.qing_guang.RemoteControl.plugin.connect;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.qing_guang.RemoteControl.packet.Packet;
import com.qing_guang.RemoteControl.packet.client.ClientExitPacket;
import com.qing_guang.RemoteControl.packet.client.ClientVerifyPacket;
import com.qing_guang.RemoteControl.packet.client.CommandLinePacket;
import com.qing_guang.RemoteControl.packet.client.LoginAccountPacket;
import com.qing_guang.RemoteControl.packet.server.AESKeyPacket;
import com.qing_guang.RemoteControl.packet.server.BackstageInfoPacket;
import com.qing_guang.RemoteControl.packet.server.EncryptRequirePacket;
import com.qing_guang.RemoteControl.packet.server.OnlineModeChangePacket;
import com.qing_guang.RemoteControl.packet.server.OnlineModeChangePacket.Type;
import com.qing_guang.RemoteControl.packet.server.PluginInfoPacket;
import com.qing_guang.RemoteControl.packet.server.RefuseOperatePacket;
import com.qing_guang.RemoteControl.packet.server.ServerInfoPacket;
import com.qing_guang.RemoteControl.packet.server.ServerVerifyPacket;
import com.qing_guang.RemoteControl.packet.server.SuccessfulLoginPacket;
import com.qing_guang.RemoteControl.packet.server.WorldInfoPacket;
import com.qing_guang.RemoteControl.plugin.connect.RemoteControlServer.DisconnectionCause;
import com.qing_guang.RemoteControl.plugin.event.ClientLoginRequestEvent;
import com.qing_guang.RemoteControl.plugin.event.ClientLoginSuccessEvent;
import com.qing_guang.RemoteControl.plugin.event.PacketSendEvent;
import com.qing_guang.RemoteControl.plugin.main.Main;
import com.qing_guang.RemoteControl.plugin.setout.Recorder;
import com.qing_guang.RemoteControl.util.CommunicateEncryptUtil;
import com.qing_guang.RemoteControl.util.channel.ConnectChannel;
import com.qing_guang.RemoteControl.util.channel.WriteChannel;

/**
 * 默认的客户端实现类
 * @author Qing_Guang
 *
 */
public class RemoteControlClient extends Thread implements Cloneable{

	private boolean isRunning;
	private String version;
	private String uname;
	private ConnectChannel channel;
	private AbstractClientExceptionHandler client_write_exc;
	private AbstractClientExceptionHandler client_read_exc;
	private RemoteControlServer server;
	
	/**
	 * 客户端运行的主方法
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		
		channel.start();
		isRunning = true;
		long timeout = 30000;
		
		try {
			
			ClientVerifyPacket cvp = inst(readPacket(false,false,timeout),ClientVerifyPacket.class,true);
			if(cvp.getVersion() == null) {
				throw new UnsupportedOperationException();
			}
			
			addPacketWillSend(new ServerVerifyPacket(JavaPlugin.getPlugin(Main.class).getDescription().getVersion()));
			addPacketWillSend(new EncryptRequirePacket());
			com.qing_guang.RemoteControl.packet.client.RSAPublicKeyPacket pp = inst(readPacket(false,false,timeout),com.qing_guang.RemoteControl.packet.client.RSAPublicKeyPacket.class,true);
			PublicKey pubkey = null;
			if(pp.getPubkey() == null || pp.getCharset() == null || (pubkey = CommunicateEncryptUtil.loadPublicKey(pp.getPubkey())) == null) {
				throw new UnsupportedOperationException();
			}else{
				KeyPair pair = CommunicateEncryptUtil.buildRSAKeyPair(512);
				String aes_key = CommunicateEncryptUtil.randomAESKey();
				channel.getWriteChannel().setAESKey(aes_key);
				channel.getWriteChannel().setRSAKey(pubkey);
				channel.getReadChannel().setAESKey(aes_key);
				channel.getReadChannel().setRSAKey(pair.getPrivate());
				com.qing_guang.RemoteControl.packet.server.RSAPublicKeyPacket rpk = new com.qing_guang.RemoteControl.packet.server.RSAPublicKeyPacket(pair.getPublic().getEncoded(),Charset.defaultCharset().displayName());
				addPacketWillSend(rpk);
				addPacketWillSend(new AESKeyPacket(aes_key),true);
			}
			
			timeout += 1000;
			LoginAccountPacket lap = inst(readPacket(true,false,timeout),LoginAccountPacket.class,true);
			if(Main.ACCOUNTS.containsKey(lap.getUname()) && Main.ACCOUNTS.get(lap.getUname()).getPwd().equals(CommunicateEncryptUtil.getMD5String(lap.getPwd()))) {
				if(!server.isOnline(lap.getUname())) {
					ClientLoginRequestEvent event = new ClientLoginRequestEvent(lap.getUname(),lap.getPwd());
					Bukkit.getPluginManager().callEvent(event);
					if(!event.isCancelled()) {
						SuccessfulLoginPacket pkt = new SuccessfulLoginPacket();
						addPacketWillSend(pkt,false);
						register(lap.getUname());
						action();
					}else {
						addPacketWillSend(new RefuseOperatePacket("Another Plugin or Console already cancelled the login request"),false);
						throw new UnsupportedOperationException();
					}
				}else {
					addPacketWillSend(new SuccessfulLoginPacket(SuccessfulLoginPacket.LoginFailureReason.ALREADY_LOGGED),false);
				}
			}else {
				addPacketWillSend(new SuccessfulLoginPacket(SuccessfulLoginPacket.LoginFailureReason.UNAME_OR_PASSWORD_WRONG),false);
			}
			
		}catch(InterruptedException e){
			try {
				channel.close();
			} catch (IOException e1) {
			}
		}catch(TimeoutException  | UnsupportedClassTypeException | UnsupportedOperationException e) {
			try {
				addPacketWillSend(new RefuseOperatePacket("Incorrect login process"));
				while(!noSendTask()) {
					Thread.sleep(20);
				}
				channel.close();
			} catch (IOException | InterruptedException e1) {
			}
			return;
		}
	}
	
	/**
	 * 读取一个数据包(阻塞)
	 * @param decrypt 是否使用解密算法解密
	 * @param rsa_or_aes 解密算法,为true则使用rsa解密,否则使用aes解密
	 * @param brk 当此Callable.call()返回false时,将会停止阻塞并返回null
	 * @return 读取到的数据包,若没读取到或brk.call()返回false时返回null
	 * @throws Exception 当brk.call()出现异常或Thread.sleep()出现InterruptedException时抛出
	 * @see com.qing_guang.RemoteControl.util.channel.ReadChannel#getPacket()
	 * @see com.qing_guang.RemoteControl.util.channel.ReadChannel#getPacket(boolean)
	 * @see java.lang.InterruptedException
	 */
	public Packet<?> readPacket(boolean decrypt,boolean rsa_or_aes,Callable<Boolean> brk) throws Exception {
		try {
			while(isRunning() && (brk != null ? brk.call() : true)) {
				Packet<?> pkt;
				if((pkt = (decrypt ? channel.getReadChannel().getPacket(rsa_or_aes) : channel.getReadChannel().getPacket())) != null) {
					return pkt;
				}
				Thread.sleep(20);
			}
		} catch (UnsupportedEncodingException | InterruptedException e) {
		} catch (Exception e) {
			throw e;
		}
		return null;
	}
	
	/**
	 * 读取一个数据包(阻塞,有最大等待时间)
	 * @param decrypt 是否使用解密算法解密
	 * @param rsa_or_aes 解密算法,为true则使用rsa解密,否则使用aes解密
	 * @param timeout 最大等待时长
	 * @return 读取到的数据包,若没读取到或brk.call()返回false时返回null
	 * @throws TimeoutException 当等待时长超过timeout时抛出
	 * @throws InterruptedException 另见java.lang.Thread.join(long)
	 * @see #readPacket(boolean,boolean,Callable)
	 * @see com.qing_guang.RemoteControl.util.channel.ReadChannel#getPacket()
	 * @see com.qing_guang.RemoteControl.util.channel.ReadChannel#getPacket(boolean)
	 * @see java.util.concurrent.TimeoutException
	 * @see java.lang.InterruptedException
	 */
	public Packet<?> readPacket(boolean decrypt,boolean rsa_or_aes,long timeout) throws TimeoutException, InterruptedException{
		
		class AnoThread1{
			Packet<?> pkt;
			boolean success = true;
		}
		AnoThread1 ano = new AnoThread1();
		Thread thread = new Thread(() -> {
			try {
				ano.pkt = readPacket(decrypt,rsa_or_aes,() -> {
					return ano.success;
				});
			} catch (Exception e) {
			}
		});
		thread.start();
		thread.join(timeout);
		if(ano.pkt == null) {
			ano.success = false;
			throw new TimeoutException();
		}
		return ano.pkt;
		
	}
	
	/**
	 * 添加一个待发送(不加密)的数据包到输出缓冲区里
	 * @param pkt 待发送的数据包
	 * @see com.qing_guang.RemoteControl.util.channel.WriteChannel#addPacketWillSend(Packet)
	 */
	public void addPacketWillSend(Packet<?> pkt) {
		
		new Thread(() -> {
			
			PacketSendEvent pse = new PacketSendEvent(pkt, false, false);
			
			Future<Object> future = Bukkit.getScheduler().callSyncMethod(Main.getPlugin(Main.class), () -> {
				Bukkit.getPluginManager().callEvent(pse);
				return 123;
			});
			try {
				future.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!pse.isCancelled()) {
				if(!pse.isEncrypt()) {
					channel.getWriteChannel().addPacketWillSend(pkt);
				}else {
					channel.getWriteChannel().addPacketWillSend(pkt,pse.encryptAlg());
				}
			}
			
		}).start();
		
	}
	
	/**
	 * 添加一个待发送(加密)的数据包到输出缓冲区里
	 * @param pkt 待发送的数据包
	 * @param rsa_or_aes 若为true则使用rsa算法加密,否则使用aes算法加密
	 * @see com.qing_guang.RemoteControl.util.channel.WriteChannel#addPacketWillSend(Packet,boolean)
	 */
	public void addPacketWillSend(Packet<?> pkt,boolean rsa_or_aes) {
		
		new Thread(() -> {
			
			PacketSendEvent pse = new PacketSendEvent(pkt, true, rsa_or_aes);
			
			Future<Object> future = Bukkit.getScheduler().callSyncMethod(Main.getPlugin(Main.class), () -> {
				Bukkit.getPluginManager().callEvent(pse);
				return 123;
			});
			
			try {
				future.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!pse.isCancelled()) {
				if(pse.isEncrypt()) {
					channel.getWriteChannel().addPacketWillSend(pkt,rsa_or_aes);
				}else {
					channel.getWriteChannel().addPacketWillSend(pkt);
				}
			}
			
		}).start();
		
	}
	
	/**
	 * 返回当前输出通道是否完成了所有发送任务
	 */
	public boolean noSendTask() {
		WriteChannel wchannel = channel.getWriteChannel();
		return !wchannel.isWriting() && wchannel.getBuffer().insertWhere() == wchannel.getBuffer().size();
	}
	
	/**
	 * 此客户端的用户名
	 */
	public String getUname() {
		return uname;
	}
	
	/**
	 * 此客户端正在使用的版本
	 */
	public String getVersion() {
		return version;
	}
	
	/**
	 * 创建此客户端的服务器
	 */
	public RemoteControlServer getServer() {
		return server;
	}
	
	/**
	 * 是否还在运行
	 */
	public boolean isRunning() {
		return isRunning;
	}
	
	void disconn() throws IOException {
		isRunning = false;
		channel.close();
	}
	
	ConnectChannel getChannel(){
		return channel;
	}
	
	void init(Socket client,AbstractClientExceptionHandler client_write_exc,AbstractClientExceptionHandler client_read_exc,RemoteControlServer server) throws IOException {
		channel = new ConnectChannel(client, client_write_exc, client_read_exc);
		this.client_write_exc = client_write_exc;
		this.client_read_exc = client_read_exc;
		this.server = server;
	}
	
	/**
	 * @see java.lang.Object#clone()
	 */
	public RemoteControlClient clone() {
		return new RemoteControlClient();
	}
	
	//注册
	private void register(String uname) {
		
		this.uname = uname;
		client_write_exc.setClient(client_write_exc.client == null ? this : client_write_exc.client);
		client_read_exc.setClient(client_read_exc.client == null ? this : client_write_exc.client);
		
		addPacketWillSend(new ServerInfoPacket(Main.SERVER_INFO),false);
		
		for(String info : Main.PLUGINS_INFO.keySet()) {
			addPacketWillSend(new PluginInfoPacket(Main.PLUGINS_INFO.get(info)),false);
		}
		
		for(String info : Main.WORLDS_INFO.keySet()) {
			addPacketWillSend(new WorldInfoPacket(Main.WORLDS_INFO.get(info)),false);
		}
		
		for(Player player : Bukkit.getOnlinePlayers()) {
			addPacketWillSend(new OnlineModeChangePacket(Type.PLAYER, true, player.getName()),false);
		}
		
		for(RemoteControlClient client : server.getClients()) {
			addPacketWillSend(new OnlineModeChangePacket(Type.CLIENT, true, client.uname),false);
		}
		
		for(String msg : Recorder.INSTANCE.getAll()) {
			addPacketWillSend(new BackstageInfoPacket(msg),false);
		}
		server.logged.put(uname, this);
		Bukkit.getScheduler().callSyncMethod(JavaPlugin.getPlugin(Main.class), () -> {
			Bukkit.getPluginManager().callEvent(new ClientLoginSuccessEvent(this));
			return null;
		});
		
	}
	
	//运行
	private void action() {
		
		Packet<?> pkt = null;
		while(isRunning()) {
			
			try {
				pkt = readPacket(true,false,null);
			} catch (Exception e) {
			}
			if(pkt instanceof CommandLinePacket) {
				CommandLinePacket clp = (CommandLinePacket) pkt;
				Bukkit.getScheduler().callSyncMethod(JavaPlugin.getPlugin(Main.class), () -> {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), clp.getCmdLine());
					return null;
				});
			}else if(pkt instanceof ClientExitPacket) {
				server.disconn(this,DisconnectionCause.CLIENT_EXIT);
			}
			
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
	}

	//判断传入的对象是否为指定类的实现类,如果是返回此对象(转型),如果不是返回null或者抛出UnsupportedClassTypeException异常
	@SuppressWarnings("unchecked")
	private static <T> T inst(Object obj,Class<T> clazz,boolean throwexc) throws UnsupportedClassTypeException{
		if(clazz.isInstance(obj)) {
			return (T)obj;
		}
		if(throwexc) {
			throw new UnsupportedClassTypeException();
		}else {
			return null;
		}
	}
	
}
