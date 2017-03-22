package pt.iscte.pandionj;

import java.util.ArrayList;
import java.util.List;

import pt.iscte.pandionj.extensibility.IObjectWidgetExtension;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.extensions.ImageWidget;
import pt.iscte.pandionj.extensions.StringWidget;
import pt.iscte.pandionj.model.ArrayModel;

public class ExtensionManager {

	static List<IArrayWidgetExtension> arrayExtensions;
	
	static List<IObjectWidgetExtension> objectExtensions;
	
	static {
		arrayExtensions = new ArrayList<>();
//		arrayPrimitiveExtensions.add(new Histogram());
		arrayExtensions.add(new ImageWidget());
		
		
		
		
		objectExtensions = new ArrayList<>();
		objectExtensions.add(new StringWidget());
	}
	
	
	static IArrayWidgetExtension getCompatibleExtension(ArrayModel m) {
		for(IArrayWidgetExtension ext : ExtensionManager.arrayExtensions)
			if(ext.accept(m))
				return ext;
		return null;
	}
}
