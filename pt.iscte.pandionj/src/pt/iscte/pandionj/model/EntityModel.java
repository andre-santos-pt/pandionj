package pt.iscte.pandionj.model;

import org.eclipse.jdt.debug.core.IJavaObject;

public abstract class EntityModel<T extends IJavaObject> extends ModelElement<T> {

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

	public abstract boolean hasWidgetExtension();
}
