package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaObject;

import pt.iscte.pandionj.ExtensionManager;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IWidgetExtension;

public abstract class EntityModel<T extends IJavaObject> extends ModelElement<T> implements IEntityModel {

//	protected IWidgetExtension extension;
	protected T entity;
	
//	private Collection<String> tags;
	
	public EntityModel(T entity, StackFrameModel model) {
		super(model);
		assert entity != null;
		this.entity = entity;
		init(entity);
//		tags = new ArrayList<>();
//		Collection<ReferenceModel> references = model.getReferencesTo(this);
//		for(ReferenceModel r : references)
//			tags.addAll(r.getTags());
	}
	
//	public Collection<String> getTags() {
//		return Collections.unmodifiableCollection(tags);
//	}

	protected abstract void init(T entity);
	
	public abstract boolean hasWidgetExtension();
	
	// TODO problem of object state
//	protected IFigure createExtensionFigure() {
//		if(extension == null)
//			extension = ExtensionManager.getExtension(this);
//		
//		return extension.createFigure(this);
//	}
	
	@Override
	public T getContent() {
		return entity;
	}
}
