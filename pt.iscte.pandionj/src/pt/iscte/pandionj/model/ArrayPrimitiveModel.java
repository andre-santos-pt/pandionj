package pt.iscte.pandionj.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IValueModel;


public class ArrayPrimitiveModel extends ArrayModel<IValueModel> {
	
	public ArrayPrimitiveModel(IJavaArray array, RuntimeModel runtime, IReferenceModel sourceReference) throws DebugException {
		super(array, runtime);
	}

	IValueModel createElement(IJavaVariable var) throws DebugException {
		return new ValueModel(var, false, true, null, getRuntimeModel());
	}
	
	public boolean isDecimal() {
		return getComponentType().matches("float|double");
	}
}
