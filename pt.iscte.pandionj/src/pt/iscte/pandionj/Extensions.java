package pt.iscte.pandionj;

import java.util.ArrayList;
import java.util.List;

import pt.iscte.pandionj.extensibility.ImageWidget2;
import pt.iscte.pandionj.extensibility.WidgetExtension;

public class Extensions {

	static List<WidgetExtension> arrayPrimitiveExtensions;
	
	static {
		arrayPrimitiveExtensions = new ArrayList<>();
//		arrayPrimitiveExtensions.add(new Histogram());
		arrayPrimitiveExtensions.add(new ImageWidget2());
	}
}
