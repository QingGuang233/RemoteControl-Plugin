package com.qing_guang.RemoteControl.plugin.main;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.qing_guang.RemoteControl.plugin.connect.RemoteControlClient;
import com.qing_guang.RemoteControl.plugin.connect.RemoteControlServer.DisconnectionCause;
import com.qing_guang.RemoteControl.util.CommunicateEncryptUtil;

import net.md_5.bungee.api.ChatColor;

/**
 * 指令处理类
 * @author Qing_Guang
 *
 */
public class Commands implements CommandExecutor{

	//rc help
	//rc reload
	//rc remove <uname>
	//rc disconn <uname>
	//rc add <uname> <pwd>
	
	/**
	 * {@inheritDoc}
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) {
		
		if(!sender.hasPermission("rc.use")) {
			sender.sendMessage(ChatColor.RED + "你没有权限使用此指令!");
		}
		
		if(args.length == 1) {
			
			if(args[0].equalsIgnoreCase("help")) {
				
				sender.sendMessage(ChatColor.GREEN + "查看本指令的帮助信息");
				sender.sendMessage(ChatColor.GREEN + "/rc reload 重新加载本插件,包括重启监听服务器等一系列操作");
				sender.sendMessage(ChatColor.GREEN + "/rc remove <用户名> 删除一个已注册的账号(无人登陆)");
				sender.sendMessage(ChatColor.GREEN + "/rc disconn <用户名> 强制下线一个正在控制的客户端");
				sender.sendMessage(ChatColor.GREEN + "/rc add <用户名> <密码> 添加一个可以登陆的账户(请在没有客户端控制的时候使用以保安全)");
				
			}else if(args[0].equalsIgnoreCase("reload")){
				
				if(!sender.hasPermission("rc.reload")) {
					Main m = JavaPlugin.getPlugin(Main.class);
					Bukkit.getPluginManager().disablePlugin(m);
					Bukkit.getPluginManager().enablePlugin(m);
					sender.sendMessage(ChatColor.GREEN + "插件已重新加载");
				}else{
					sender.sendMessage(ChatColor.RED + "你没有权限使用此指令!");
				}
				
			}else {
				return Bukkit.dispatchCommand(sender, "rc help");
			}
			
		}else if(args.length == 2) {
			
			Account acc = Main.ACCOUNTS.get(args[1]);
			RemoteControlClient client = Main.server.getClient(args[1]);
			
			if(args[0].equalsIgnoreCase("remove")) {
				
				if(sender.hasPermission("rc.acc_ctrl")) {
					if(acc != null && client == null) {
						
						Main.ACCOUNTS.remove(args[1]);
						sender.sendMessage(ChatColor.GREEN + "此账户已被注销");
						
					}else if(acc == null) {
						sender.sendMessage(ChatColor.RED + "此账户没有被注册");
					}else {
						sender.sendMessage(ChatColor.RED + "此账户在线上");
					}
				}else{
					sender.sendMessage(ChatColor.RED + "你没有权限使用此指令!");
				}
				
			}else if(args[0].equalsIgnoreCase("disconn")) {
				
				if(sender.hasPermission("rc.disconn")) {
					if(acc != null && client != null) {
						
						Main.server.disconn(client, DisconnectionCause.MANUAL);
						sender.sendMessage(ChatColor.GREEN + "此账户已被强制下线");
						
					}else if(acc == null) {
						sender.sendMessage(ChatColor.RED + "此账户没有被注册");
					}else {
						sender.sendMessage(ChatColor.RED + "此账户不在线上");
					}
				}else{
					sender.sendMessage(ChatColor.RED + "你没有权限使用此指令!");
				}
				
			}else {
				return Bukkit.dispatchCommand(sender, "rc help");
			}
			
		}else if(args.length == 3) {
			
			Account acc = Main.ACCOUNTS.get(args[1]);
//			RemoteControlClient client = Main.server.getClient(args[1]);
			
			if(args[0].equalsIgnoreCase("add")) {
				
				if(sender.hasPermission("rc.acc_ctrl")) {
					if(acc == null) {
						
						Main.ACCOUNTS.put(args[1], new Account(args[1], CommunicateEncryptUtil.getMD5String(args[2])));
						sender.sendMessage(ChatColor.GREEN + "此账户已被注册");
						
					}else if(acc != null) {
						sender.sendMessage(ChatColor.RED + "此账户已被注册");
					}
				}else{
					sender.sendMessage(ChatColor.RED + "你没有权限使用此指令!");
				}
				
			}else {
				return Bukkit.dispatchCommand(sender, "rc help");
			}
			
		}else {
			return Bukkit.dispatchCommand(sender, "rc help");
		}
		
		return true;
		
	}

}
