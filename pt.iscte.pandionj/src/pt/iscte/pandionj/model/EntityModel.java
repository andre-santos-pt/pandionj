package pt.iscte.pandionj.model;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.debug.core.IJavaObject;

import pt.iscte.pandionj.ExtensionManager;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IWidgetExtension;

public abstract class EntityModel<T extends IJavaObject> extends ModelElement<T> implements IEntityModel {

	protected IWidgetExtension extension;
	protected T entity;
	
	public EntityModel(T entity, StackFrameModel model) {
		super(model);
		assert entity != null;
		this.entity = entity;
		init(entity);
		extension = ExtensionManager.getExtension(this);
	}

	protected abstract void init(T entity);
	
	public boolean hasWidgetExtension() {
		return extension != ExtensionManager.NO_EXTENSION;
	}
	
	
	// TODO problem of object state
	protected IFigure createExtensionFigure() {
		if(extension == null)
			extension = ExtensionManager.getExtension(this);
		
		return extension.createFigure(this);
	}
	
	
	
	@Override
	public T getContent() {
		return entity;
	}

}
