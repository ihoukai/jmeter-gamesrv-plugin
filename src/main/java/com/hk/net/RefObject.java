package com.hk.net;

/**
 * This class is used to simulate the ability to pass arguments by reference in Java.
 * @param <T>
 */
public final class RefObject<T>
{
	public T argValue;
	public RefObject(T refArg)
	{
		argValue = refArg;
	}
}