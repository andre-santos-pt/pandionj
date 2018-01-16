package pt.iscte.pandionj;

import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.extensibility.PandionJConstants;

public interface Utils {

	static String toSimpleName(String qualifiedName) {
		String name = qualifiedName;
		int dot = name.lastIndexOf('.');
		if(dot != -1) 
			name = name.substring(dot+1);

		int dollar = name.lastIndexOf('$');
		if(dollar != -1)
			name = name.substring(dollar+1);

		name = name.replaceFirst("", "");
		return name;
	}

	static void stripQualifiedNames(List<String> list) {
		for(int i = 0; i < list.size(); i++)
			list.set(i, toSimpleName(list.get(i)));
	}

	static void addArrowDecoration(PolylineConnection pointer) {
		PolylineDecoration decoration = new PolylineDecoration();
		PointList points = new PointList();
		points.addPoint(-1, -1);
		points.addPoint(0, 0);
		points.addPoint(-1, 1);
		decoration.setTemplate(points);
		decoration.setScale(PandionJConstants.ARROW_EDGE, PandionJConstants.ARROW_EDGE);
		decoration.setLineWidth(PandionJConstants.ARROW_LINE_WIDTH);
		decoration.setOpaque(true);
		pointer.setTargetDecoration(decoration);
	}

	//	private static void addNullDecoration(PolylineConnection pointer) {
	//	PolygonDecoration decoration = new PolygonDecoration();
	//	PointList points = new PointList();
	//	points.addPoint(0,-1); // 1
	//	points.addPoint(0, 1); // -1
	//	decoration.setTemplate(points);
	//	decoration.setScale(Constants.ARROW_EDGE, Constants.ARROW_EDGE);
	//	decoration.setLineWidth(Constants.ARROW_LINE_WIDTH);
	//	decoration.setOpaque(true);
	//	pointer.setTargetDecoration(decoration);	
	//}
	
	static String getTooltip(IVariableModel<?> model) {
		IJavaVariable javaVariable = model.getJavaVariable();
		String owner = null;
		if(javaVariable instanceof IJavaFieldVariable)
			try {
				owner = ((IJavaFieldVariable) javaVariable).getDeclaringType().getName();
			} catch (DebugException e) { }
		String tooltip = model.isStatic() ? "static field" : (owner == null ? "local variable" : "field");
		
		if(model.isStatic() && owner != null)
			tooltip += " of " + owner;
		
		tooltip += " (" + model.getTypeName() + ")";
		return tooltip;
	}
	

}
