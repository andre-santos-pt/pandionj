package pt.iscte.pandionj.model;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.swt.widgets.Display;

public abstract class ModelElement<T extends IJavaValue> extends DisplayUpdateObservable {
	private RuntimeModel runtime;
	
	public ModelElement(RuntimeModel runtime) {
		assert runtime != null;
		this.runtime = runtime;
	}

	public RuntimeModel getRuntimeModel() {
		return runtime;
	}

	public abstract T getContent();

	public abstract boolean update(int step);

	public abstract void setStep(int stepPointer);
	
	
}