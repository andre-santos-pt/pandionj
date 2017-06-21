package pt.iscte.pandionj.model;

import org.eclipse.jdt.debug.core.IJavaObject;

public class NullModel extends EntityModel<IJavaObject> {

	public NullModel(RuntimeModel runtime) {
		super((IJavaObject) runtime.getDebugTarget().nullValue(), runtime);
	}

	@Override
	public boolean update(int step) {
		return false;
	}

	@Override
	public String toString() {
		return "NULL";
	}

	@Override
	public boolean hasWidgetExtension() {
		return false;
	}

	@Override
	public void setStep(int stepPointer) {

	}
}
