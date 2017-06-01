package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.figures.ArrayPrimitiveFigure;


// TODO limit size?
public class ArrayPrimitiveModel extends ArrayModel {

	private List<ValueModel> values;
	
	public ArrayPrimitiveModel(IJavaArray array, StackFrameModel model) {
		super(array, model);
	}

	@Override
	protected void initArray(IJavaArray array) {
		try {
			values = new ArrayList<>(array.getLength());
			for(int i = 0; i < array.getLength(); i++) {
				ValueModel m = new ValueModel((IJavaVariable) array.getVariable(i), false, getStackFrame());
				values.add(m);
			}
		} catch (DebugException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isDecimal() {
		return getComponentType().matches("float|double");
	}
	
	@Override
	protected IFigure createArrayFigure() {
		return new ArrayPrimitiveFigure(this);
	}
	
	boolean updateInternal(int i, int step) {
		return values.get(i).update(step);
	}
	
	public ValueModel getElementModel(int index) {
		return values.get(index);
	}
}
