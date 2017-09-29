package pt.iscte.pandionj.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaValue;

public abstract class ModelElement<T extends IJavaValue,O> extends DisplayUpdateObservable<O> {
	private RuntimeModel runtime;
	
	public ModelElement(RuntimeModel runtime) {
		assert runtime != null;
		this.runtime = runtime;
	}

	public RuntimeModel getRuntimeModel() {
		return runtime;
	}

	public abstract T getContent();

	public abstract boolean update(int step) throws DebugException;

	public abstract void setStep(int stepPointer);
}