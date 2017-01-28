package pt.iscte.pandionj.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.Figure;
import org.eclipse.jdt.debug.core.IJavaValue;

import pt.iscte.pandionj.figures.StringFigure;

public class StringHandler implements TypeHandler {


	@Override
	public boolean qualifies(IJavaValue v) {
		try {
			return !v.isNull() && v.getJavaType().getName().equals(String.class.getName());
		} catch (DebugException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isValueType() {
		return true;
	}

	@Override
	public boolean expandLinks(ModelElement e) {
		return false;
	}
	
	@Override
	public Figure createFigure(ModelElement e) {
		try {
			return new StringFigure(e.getContent().getValueString());
		} catch (DebugException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@Override
	public String getTextualValue(IJavaValue v) {
		try {
			return v.getValueString();
		} catch (DebugException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	

}
