package pt.iscte.pandionj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaObject;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectWidgetExtension;
import pt.iscte.pandionj.extensibility.IWidgetExtension;
import pt.iscte.pandionj.extensions.ColorAguia;
import pt.iscte.pandionj.extensions.ColorWidget;
import pt.iscte.pandionj.extensions.ImageAguia;
import pt.iscte.pandionj.extensions.GrayscaleImageWidget;
import pt.iscte.pandionj.extensions.IterableWidget;
import pt.iscte.pandionj.extensions.StringWidget;
import pt.iscte.pandionj.model.ArrayModel;
import pt.iscte.pandionj.model.ObjectModel;

public class ExtensionManager {

	private static Map<String, IArrayWidgetExtension> arrayExtensions;
	private static List<IObjectWidgetExtension> objectExtensions;
	
	static {
		arrayExtensions = new HashMap<String, IArrayWidgetExtension>();
		arrayExtensions.put("image", new GrayscaleImageWidget());
		
		objectExtensions = new ArrayList<>();
		objectExtensions.add(new StringWidget());
		objectExtensions.add(new ColorWidget());
		objectExtensions.add(new IterableWidget());
		objectExtensions.add(new ColorAguia());
		objectExtensions.add(new ImageAguia());
	}
	
	
//	public static <T extends IEntityModel> IWidgetExtension<?> getExtension(T e) {
//		if(e instanceof ArrayModel)
//			return getArrayExtension((ArrayModel) e);
//		else if(e instanceof ObjectModel)
//			return getObjectExtension((ObjectModel) e);
//		else
//			return NO_EXTENSION;
//	}
	
	public static IArrayWidgetExtension getArrayExtension(ArrayModel m) {
		for (String tag : m.getTags()) {
			IArrayWidgetExtension ext = arrayExtensions.get(tag);
			if(ext != null && ext.accept(m))
				return ext;
		}
		return IArrayWidgetExtension.NULL_EXTENSION;
	}


	public static IObjectWidgetExtension getObjectExtension(ObjectModel m) {
		IType type = m.getType();
		for(IObjectWidgetExtension ext : ExtensionManager.objectExtensions)
			if(ext.accept(type))
				return ext;
		return IObjectWidgetExtension.NULL_EXTENSION;
	}
	
}
