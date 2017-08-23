package com.hk.net;


public enum PacketType
{
	Handshake(0x01),
	HandshakeACK(0x02),
	Heartbeat(0x03),
	TransData(0x04),
	ConnectionClose(0x05);

	private int intValue;
	private static java.util.HashMap<Integer, PacketType> mappings;
	private static java.util.HashMap<Integer, PacketType> getMappings()
	{
		if (mappings == null)
		{
			synchronized (PacketType.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, PacketType>();
				}
			}
		}
		return mappings;
	}

	private PacketType(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static PacketType forValue(int value)
	{
		return getMappings().get(value);
	}
}