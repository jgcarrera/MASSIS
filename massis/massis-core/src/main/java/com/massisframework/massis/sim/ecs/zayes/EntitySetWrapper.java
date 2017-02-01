package com.massisframework.massis.sim.ecs.zayes;

import java.util.Iterator;
import java.util.Set;

import com.simsilica.es.EntityId;

public class EntitySetWrapper implements SimulationEntitySet {

	private InterfaceEntitySet es;

	public EntitySetWrapper(InterfaceEntitySet es)
	{
		this.es = es;
	}

	@Override
	public boolean containsId(EntityId id)
	{
		return es.containsId(id);
	}

	@Override
	public Set<EntityId> getEntityIds()
	{
		return es.getEntityIds();
	}

	@Override
	public SimulationEntity getEntity(EntityId id)
	{
		return (SimulationEntity) es.getEntity(id);
	}

	@Override
	public Set<SimulationEntity> getAddedEntities()
	{
		return (Set) es.getAddedEntities();
	}

	@Override
	public Set<SimulationEntity> getChangedEntities()
	{
		return (Set) es.getChangedEntities();
	}

	@Override
	public Set<SimulationEntity> getRemovedEntities()
	{
		return (Set) es.getRemovedEntities();
	}

	@Override
	public boolean hasChanges()
	{
		return es.hasChanges();
	}

	@Override
	public boolean applyChanges()
	{
		return es.applyChanges();
	}

	@Override
	public void release()
	{
		es.release();
	}

	@Override
	public boolean hasType(Class type)
	{
		return es.hasType(type);
	}

	@Override
	public Iterator<SimulationEntity> iterator()
	{
		return (Iterator) es.iterator();
	}
}
