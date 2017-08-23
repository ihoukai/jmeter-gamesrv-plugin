package com.hk.net;

public enum PackageType
{
	PKG_HANDSHAKE(1),
	PKG_HANDSHAKE_ACK(2),
	PKG_HEARTBEAT(3),
	PKG_DATA(4),
	PKG_KICK(5);

	private int intValue;
	private static java.util.HashMap<Integer, PackageType> mappings;
	private static java.util.HashMap<Integer, PackageType> getMappings()
	{
		if (mappings == null)
		{
			synchronized (PackageType.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, PackageType>();
				}
			}
		}
		return mappings;
	}

	private PackageType(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static PackageType forValue(int value)
	{
		return getMappings().get(value);
	}
}