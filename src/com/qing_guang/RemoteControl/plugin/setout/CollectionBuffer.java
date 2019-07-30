package com.qing_guang.RemoteControl.plugin.setout;

import com.qing_guang.RemoteControl.util.Buffer;

/**
 * 实现一个限制可容纳大小的缓冲区
 * @author Qing_Guang
 * @param <E> 缓冲区容纳的数据类型
 * @see com.qing_guang.RemoteControl.util.Buffer
 */
public class CollectionBuffer<E> extends Buffer<E>{
	
	private int size;
	
	/**
	 * 新建一个指定容量的缓冲区
	 * @param size 容量大小
	 * @throws IllegalArgumentException 当传入的大小小于0时抛出
	 */
	public CollectionBuffer(int size) throws IllegalArgumentException{
		if(size < 0) {
			throw new IllegalArgumentException("The buffer size can't less than 0");
		}
		this.size = size;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void add(E e) {
		super.add(e);
		if(size() > size) {
			insertTo(insertWhere() + 1);
			clear(1);
		}
	}
	
}
