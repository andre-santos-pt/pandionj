package pt.iscte.pandionj.model;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaObject;

import pt.iscte.pandionj.extensibility.IWidgetExtension;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;

public abstract class EntityModel<T extends IJavaObject> extends ModelElement<T> implements IEntityModel {

	private IWidgetExtension extension;
	
	public EntityModel(StackFrameModel model) {
		super(model);
	}

	public void setWidgetExtension(IWidgetExtension extension) {
		this.extension = extension;
	}
	
	public boolean hasWidgetExtension() {
		return extension != null;
	}
	
	protected IFigure createExtensionFigure() {
		return extension.createFigure(this);
	}
	

}
