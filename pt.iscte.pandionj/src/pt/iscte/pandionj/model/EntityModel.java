package pt.iscte.pandionj.model;

import org.eclipse.jdt.debug.core.IJavaObject;

import pt.iscte.pandionj.extensibility.IEntityModel;

public abstract class EntityModel<T extends IJavaObject> extends ModelElement<T> implements IEntityModel {

	private T entity;
	
	public EntityModel(T entity, StackFrameModel model) {
		super(model);
		assert entity != null;
		this.entity = entity;
		init(entity);
	}
	

	protected abstract void init(T entity);
	
	public abstract boolean hasWidgetExtension();
	
	
	@Override
	public T getContent() {
		return entity;
	}
}
