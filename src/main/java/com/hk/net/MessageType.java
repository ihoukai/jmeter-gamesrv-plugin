package com.hk.net;

public enum MessageType
{
	MSG_REQUEST(0),
	MSG_NOTIFY(1),
	MSG_RESPONSE(2),
	MSG_PUSH(3);

	private int intValue;
	private static java.util.HashMap<Integer, MessageType> mappings;
	private static java.util.HashMap<Integer, MessageType> getMappings()
	{
		if (mappings == null)
		{
			synchronized (MessageType.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, MessageType>();
				}
			}
		}
		return mappings;
	}

	private MessageType(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static MessageType forValue(int value)
	{
		return getMappings().get(value);
	}
}