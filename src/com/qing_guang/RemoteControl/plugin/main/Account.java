package com.qing_guang.RemoteControl.plugin.main;

/**
 * 账户类
 * @author Qing_Guang
 *
 */
public class Account {

	private String uname;
	private String pwd;
	
	/**
	 * 创建一个账户类
	 * @param uname 用户名
	 * @param pwd 密码(已用md5加密)
	 */
	public Account(String uname, String pwd) {
		this.uname = uname;
		this.pwd = pwd;
	}

	/**
	 * 用户名
	 */
	public String getUname() {
		return uname;
	}

	/**
	 * 密码(已用md5加密)
	 */
	public String getPwd() {
		return pwd;
	}

	/**
	 * 判断两个账户对象是否相等
	 * @param obj 需要判断相等的账户对象
	 * @return 是否相等
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Account obj) {
		return obj.uname.equals(uname) && obj.pwd.equals(pwd);
	}

	/**
	 * @return 转成字符串之后的本对象信息
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Account [uname=" + uname + ", pwd=" + pwd + "]";
	}
	
}
