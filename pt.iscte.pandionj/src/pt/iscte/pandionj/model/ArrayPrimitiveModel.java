package pt.iscte.pandionj.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IValueModel;


public class ArrayPrimitiveModel extends ArrayModel<IValueModel> {

	
	public ArrayPrimitiveModel(IJavaArray array, RuntimeModel runtime, IReferenceModel sourceReference) {
		super(array, runtime);
	}

	IValueModel createElement(IJavaVariable var) throws DebugException {
		return new ValueModel(var, false, null, getRuntimeModel());
	}
	
	public boolean isDecimal() {
		return getComponentType().matches("float|double");
	}
	
//	boolean updateInternal(int i, int step) {
//		assert i >= 0 && i < values.size();
//		return values.get(i).update(step);
//	}
	
//	public ValueModel getElementModel(int index) {
//		assert index >= 0 && index < values.size()-1 || index == getLength()-1;
//		return values.get(index);
//	}
//	
//	@Override
//	public void setStep(int stepPointer) {
//		for(ValueModel val : values)
//			val.setStep(stepPointer);
//	}
}
