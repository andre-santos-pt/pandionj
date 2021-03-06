package pt.iscte.pandionj.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaObject;

import pt.iscte.pandionj.extensibility.IEntityModel;

public abstract class EntityModel<T extends IJavaObject> 
extends ModelElement<T, Object> 
implements IEntityModel {

	private final T entity;
	
	public EntityModel(T entity, RuntimeModel runtime) {
		super(runtime);
		assert entity != null;
		this.entity = entity;
	}
	
	@Override
	public T getContent() {
		return entity;
	}
	
	public boolean isAllocated() throws DebugException {
		return getContent().isAllocated();
	}
}
