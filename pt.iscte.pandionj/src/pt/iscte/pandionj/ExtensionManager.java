package pt.iscte.pandionj;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.core.IType;

import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectWidgetExtension;
import pt.iscte.pandionj.extensibility.IWidgetExtension;
import pt.iscte.pandionj.extensions.ColorAguia;
import pt.iscte.pandionj.extensions.ColorWidget;
import pt.iscte.pandionj.extensions.ImageAguia;
import pt.iscte.pandionj.extensions.ImageWidget;
import pt.iscte.pandionj.extensions.IterableWidget;
import pt.iscte.pandionj.extensions.StringWidget;
import pt.iscte.pandionj.model.ArrayModel;
import pt.iscte.pandionj.model.ObjectModel;

public class ExtensionManager {

	private static List<IArrayWidgetExtension> arrayExtensions;
	
	private static List<IObjectWidgetExtension> objectExtensions;
	
	public static final IWidgetExtension<IEntityModel> NO_EXTENSION = new NullExtension();
	
	public static final IObjectWidgetExtension NO_EXTENSION_OBJECT = new ObjectNullExtension();
	
	private static class NullExtension implements IWidgetExtension<IEntityModel> {
		@Override
		public IFigure createFigure(IEntityModel e) {
			return null;
		}		
	}
	
	private static class ObjectNullExtension implements IObjectWidgetExtension {
		

		@Override
		public boolean accept(IType objectType) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public IFigure createFigure(IObjectModel e) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public boolean includeMethod(String methodName) {
			return true;
		}
		
	}
	
	
	static {
		arrayExtensions = new ArrayList<>();
//		arrayExtensions.add(new ImageWidget());
		
		
		objectExtensions = new ArrayList<>();
		objectExtensions.add(new StringWidget());
		objectExtensions.add(new ColorWidget());
		objectExtensions.add(new IterableWidget());
		objectExtensions.add(new ColorAguia());
		objectExtensions.add(new ImageAguia());
	}
	
	
	public static <T extends IEntityModel> IWidgetExtension<?> getExtension(T e) {
		if(e instanceof ArrayModel)
			return getCompatibleExtension((ArrayModel) e);
		else if(e instanceof ObjectModel)
			return getCompatibleExtension((ObjectModel) e);
		else
			return NO_EXTENSION;
	}
	
	private static IWidgetExtension<?> getCompatibleExtension(ArrayModel m) {
		
		for(IArrayWidgetExtension ext : ExtensionManager.arrayExtensions)
			if(ext.accept(m))
				return ext;
		return NO_EXTENSION;
	}


	private static IWidgetExtension<?> getCompatibleExtension(ObjectModel m) {
		IType type = m.getType();
		for(IObjectWidgetExtension ext : ExtensionManager.objectExtensions)
			if(ext.accept(type))
				return ext;
		return NO_EXTENSION_OBJECT;
	}
	
}
