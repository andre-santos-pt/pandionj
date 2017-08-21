package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.extensibility.IValueModel;


public class ArrayPrimitiveModel extends ArrayModel<IValueModel> {

	private List<ValueModel> values;
	
	public ArrayPrimitiveModel(IJavaArray array, RuntimeModel runtime) {
		super(array, runtime);
	}

	@Override
	protected void initArray(IJavaArray array, int length) {
		try {
			values = new ArrayList<>(length);
			for(int i = 0; i < length - 1; i++)
				handlePosition((IJavaVariable) array.getVariable(i));

			if(length > 0)
				handlePosition((IJavaVariable) array.getVariable(length-1));
			
		} catch (DebugException e) {
			e.printStackTrace();
		}
	}
	
	private void handlePosition(IJavaVariable var) throws DebugException {
		ValueModel m = new ValueModel(var, false, null, getRuntimeModel());
		values.add(m);
	}
	
	public boolean isDecimal() {
		return getComponentType().matches("float|double");
	}
	
	boolean updateInternal(int i, int step) {
		assert i >= 0 && i < values.size();
		return values.get(i).update(step);
	}
	
	public ValueModel getElementModel(int index) {
		assert index >= 0 && index < values.size();
		return values.get(index);
	}
	
	@Override
	public void setStep(int stepPointer) {
		for(ValueModel val : values)
			val.setStep(stepPointer);
	}
}
