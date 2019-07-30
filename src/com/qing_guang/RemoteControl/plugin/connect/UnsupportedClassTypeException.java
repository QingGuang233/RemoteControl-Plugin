package com.qing_guang.RemoteControl.plugin.connect;

/**
 * 当对象的类型不支持时抛出
 * @author Qing_Guang
 *
 */
public class UnsupportedClassTypeException extends RuntimeException{

	private static final long serialVersionUID = 5354968332697191143L;
	
	/**
	 * @see java.lang.RuntimeException#RuntimeException()
	 */
	public UnsupportedClassTypeException() {
		super();
	}
	
	/**
	 * @see java.lang.RuntimeException#RuntimeException(String)
	 */
	public UnsupportedClassTypeException(String message) {
		super(message);
	}

}
