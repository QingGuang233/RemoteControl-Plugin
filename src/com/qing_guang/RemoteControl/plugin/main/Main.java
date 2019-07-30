package com.qing_guang.RemoteControl.plugin.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.qing_guang.RemoteControl.info.PluginInfo;
import com.qing_guang.RemoteControl.info.ServerInfo;
import com.qing_guang.RemoteControl.info.WorldInfo;
import com.qing_guang.RemoteControl.plugin.connect.ClientCloseExceptionHandler;
import com.qing_guang.RemoteControl.plugin.connect.ClientInputExceptionHandler;
import com.qing_guang.RemoteControl.plugin.connect.ClientOutputExceptionHandler;
import com.qing_guang.RemoteControl.plugin.connect.RemoteControlClient;
import com.qing_guang.RemoteControl.plugin.connect.RemoteControlServer;
import com.qing_guang.RemoteControl.plugin.connect.ServerCannotCloseExceptionHandler;
import com.qing_guang.RemoteControl.plugin.connect.ServerCannotInitExceptionHandler;
import com.qing_guang.RemoteControl.plugin.setout.Recorder;
import com.qing_guang.RemoteControl.util.FinalValues;
import com.qing_guang.RemoteControl.util.JsonUtil;

/**
 * 本插件主类
 * @author Qing_Guang
 *
 */
public class Main extends JavaPlugin{

	static Logger logger;
	static YamlConfiguration config;
	
	static RemoteControlServer server;
	
	/**
	 * 已注册的账户
	 */
	public static final Map<String,Account> ACCOUNTS = new HashMap<>();
	/**
	 * 服务器基本信息
	 */
	public static final ServerInfo SERVER_INFO = new ServerInfo();
	/**
	 * 所有插件的基本信息
	 */
	public static final Map<String,PluginInfo> PLUGINS_INFO = new HashMap<>();
	/**
	 * 所有世界的基本信息
	 */
	public static final Map<String,WorldInfo> WORLDS_INFO = new HashMap<>();
	
	/**
	 * {@inheritDoc}
	 */
	public void onEnable() {
		
		logger = getLogger();
		
		logger.info("感谢您使用本插件,插件版本 " + getDescription().getVersion());
		logger.info("正在初始化插件...");
		logger.info("正在加载插件所需要的文件...");
		
		try {
			
			config = load("config.yml");
			
			File acsfile = new File(getDataFolder(),"accounts.json");
			if(!acsfile.exists()) {
				acsfile.createNewFile();
				String str = "[{\"" + FinalValues.JSON_TEXT_KEY_LOGIN_ACCOUNT_UNAME + "\":\"75fe8a5d\",\"" + FinalValues.JSON_TEXT_KEY_LOGIN_ACCOUNT_PASSWORD + "\":\"b6835d01\"}]";
				FileOutputStream fos = new FileOutputStream(acsfile);
				fos.write(str.getBytes());
				fos.flush();
				fos.close();
			}
			JsonArray array = JsonUtil.toJsonObject(new FileReader(acsfile));
			if(array != null) {
				for(int i = 0;i < array.size();i++) {
					JsonObject account = array.get(i).getAsJsonObject();
					String uname = account.get(FinalValues.JSON_TEXT_KEY_LOGIN_ACCOUNT_UNAME).getAsString();
					String pwd = account.get(FinalValues.JSON_TEXT_KEY_LOGIN_ACCOUNT_PASSWORD).getAsString();
					ACCOUNTS.put(uname, new Account(uname,pwd));
				}
			}
			
		}catch(IOException e) {
			logger.warning("加载文件时出现异常!请把以下信息反馈给作者!");
			e.printStackTrace();
		}
		
		logger.info("正在获取插件所需要的服务器信息...");
		loadServerInfo(SERVER_INFO);
		loadPluginInfo(PLUGINS_INFO);
		for(World world : Bukkit.getWorlds()) {
			WORLDS_INFO.put(world.getName(), getWorldInfo(world));
		}
		
		logger.info("正在开启服务器...");
		server = new RemoteControlServer(config.getInt("port"), config.getInt("max_client"), new RemoteControlClient()
				, new ServerCannotInitExceptionHandler(logger), new ServerCannotCloseExceptionHandler(logger)
				, new ClientOutputExceptionHandler(logger), new ClientInputExceptionHandler(logger)
				, new ClientCloseExceptionHandler(logger));
		server.start();
		
		logger.info("正在做最后的准备...");
		Recorder.init();
		getCommand("rc").setExecutor(new Commands());
		Bukkit.getPluginManager().registerEvents(new EventListener(), this);
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void onDisable() {
		
		((org.apache.logging.log4j.core.Logger)LogManager.getLogger(LogManager.ROOT_LOGGER_NAME)).removeAppender(Recorder.APPENDER);
		server.disable();
		
		try {
			
			JsonArray accs = new JsonArray();
			for(String uname : ACCOUNTS.keySet()) {
				JsonObject acc = new JsonObject();
				acc.addProperty("uname", uname);
				acc.addProperty("pwd", ACCOUNTS.get(uname).getPwd());
				accs.add(acc);
			}
			
			FileOutputStream fos = new FileOutputStream(new File(getDataFolder(),"accounts.json"));
			fos.write(accs.toString().getBytes());
			fos.flush();
			fos.close();
			
		} catch (IOException e) {
			logger.warning("保存文件时出现问题!请把以下信息反馈给作者!");
			e.printStackTrace();
		}
		
		logger.info("插件已成功关闭");
		
	}
	
	/**
	 * 获得本插件的监听服务器
	 */
	public static RemoteControlServer getPluginServer() {
		return server;
	}
	
	/**
	 * 加载一个文件,如果在Main.getDataFolder()里不存在的话就从本插件内置的文件里加载
	 * @param path 在Main.getDataFolder()里面的路径
	 * @return YamlConfiguration对象
	 * @throws IOException 当读取时出现 I/O 异常时抛出
	 * @see org.bukkit.plugin.java.JavaPlugin#getDataFolder()
	 * @see org.bukkit.plugin.java.JavaPlugin#saveResource(File,boolean)
	 * @see org.bukkit.configuration.file.YamlConfiguration#loadConfiguration(File)
	 */
	public YamlConfiguration load(String path) throws IOException {
		
		File file = new File(getDataFolder(),path);
		
		if(!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
			saveResource(path, true);
		}
		
		return YamlConfiguration.loadConfiguration(file);
		
	}
	
	/**
	 * 加载一个ServerInfo对象
	 * @param info 需要加载的ServerInfo对象
	 */
	public void loadServerInfo(ServerInfo info) {
		
		info.default_gamemode(Bukkit.getDefaultGameMode().toString())
			.version((Bukkit.getServerName().equalsIgnoreCase("craftbukkit") ? "CraftBukkit/Spigot" : Bukkit.getServerName()) + Bukkit.getBukkitVersion())
			.motd(Bukkit.getMotd())
			.port(Bukkit.getPort())
			.max_player(Bukkit.getMaxPlayers())
			.view_distance(Bukkit.getViewDistance())
			.allow_nether(Bukkit.getAllowNether())
			.allow_end(Bukkit.getAllowEnd())
			.allow_fly(Bukkit.getAllowFlight())
			.is_hardcore(Bukkit.isHardcore());
		
	}
	
	/**
	 * 加载传入的所有PluginInfo对象
	 * @param map PluginInfo集合
	 */
	public void loadPluginInfo(Map<String,PluginInfo> map){
		
		Method jarFile = null;
		try {
			jarFile = JavaPlugin.class.getDeclaredMethod("getFile");
			jarFile.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e1) {
			e1.printStackTrace();
		}
		
		for(Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			
			PluginInfo info = new PluginInfo();
			
			PluginDescriptionFile desc = plugin.getDescription();
			
			String path = null;
			try {
				path = jarFile.invoke(plugin).toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			info.name(plugin.getName())
				.version(desc.getVersion())
				.website(desc.getWebsite())
				.jar_path(path)
				.datafolder_path(plugin.getDataFolder().toString());
			
			Map<String,String> pmap = new HashMap<String,String>();
			if(desc.getPermissions() != null) {
				for(Permission pmss : desc.getPermissions()) {
					pmap.put(pmss.getName(), pmss.getDescription());
				}
			}
			info.pmss(pmap);
			
			List<String> cmds = new ArrayList<>();
			if(desc.getCommands() != null) {
				for(String cmd : desc.getCommands().keySet()) {
					cmds.add(cmd);
				}
			}
			info.reged_cmds(cmds);
			info.enable(plugin.isEnabled());
			
			map.put(info.getName(), info);
			
		}
		
	}
	
	/**
	 * 新建一个世界所对应的WorldInfo对象
	 * @param world 需要加载的世界
	 * @return WorldInfo对象
	 */
	public WorldInfo getWorldInfo(World world) {
		
		Location spawn = world.getSpawnLocation();
		
		return new WorldInfo().name(world.getName())
							  .diffculty(world.getDifficulty().toString())
							  .environment(world.getEnvironment().toString())
							  .uid(world.getUID())
							  .max_height(world.getMaxHeight())
							  .seed(world.getSeed())
							  .spawn_loc_x(spawn.getX())
							  .spawn_loc_y(spawn.getY())
							  .spawn_loc_z(spawn.getZ());
		
	}
	
}
