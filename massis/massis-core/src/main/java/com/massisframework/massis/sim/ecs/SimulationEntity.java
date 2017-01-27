package com.massisframework.massis.sim.ecs;

import org.omg.CosNaming.NameComponent;

public interface SimulationEntity {

	public int getId();

	/**
	 * Taken from
	 * https://github.com/yonik/java_util/blob/master/src/util/hash/MurmurHash3.java
	 * 
	 * @param h
	 *            the key to hash
	 * @return the hashed value
	 */
	public static int fmix32(int h)
	{
		h ^= h >>> 16;
		h *= 0x85ebca6b;
		h ^= h >>> 13;
		h *= 0xc2b2ae35;
		h ^= h >>> 16;
		return h;
	}

	public <T extends SimulationComponent> T addComponent(Class<T> type);

	public <T extends SimulationComponent> void deleteComponent(Class<T> type);
}
