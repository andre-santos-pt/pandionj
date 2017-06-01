package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

public abstract class VariableModel<T extends IJavaValue> extends ModelElement<T> {
	
	protected final IJavaVariable variable;
	private String type;
	private String name;
	private final boolean isInstance;
	private boolean outOfScope;
	private List<StepValue> history;
	
	private int stepInit;
	
	private int stepPointer;
	
	private class StepValue {
		final T value;
		final int step;
		
		StepValue(T value, int step) {
			this.value = value;
			this.step = step;
		}
	}
	
	@SuppressWarnings("unchecked")
	public VariableModel(IJavaVariable variable, boolean isInstance, StackFrameModel model) {
		super(model);
		assert variable != null;
		
		this.variable = variable;
		this.isInstance = isInstance;
		history = new ArrayList<>();

		stepInit = model.getStep();
		try {
			this.type = variable.getReferenceTypeName();
			this.name = variable.getName();
			T val = (T) variable.getValue();
			StepValue sv = new StepValue(val, stepInit);
			history.add(sv);
			stepPointer = 0;
			
		} catch (DebugException e) {
			e.printStackTrace();
			
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean update(int step) {
		try {
			StepValue current = history.get(history.size()-1);
			boolean equals = variable.getValue().equals(current.value);
			if(!equals) {
				StepValue sv = new StepValue((T) variable.getValue(), step);
				history.add(sv);
				stepPointer++;
				setChanged();
				notifyObservers();
				return true;
			}
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public String getName() {		
		return name;
	}
	
	public boolean isInstance() {
		return isInstance;
	}
	
	public String getType() {
		return type;
	}
	
	public void setStep(int step) {
		if(stepPointer == -1 || step > history.get(stepPointer).step) {
			do {
				stepPointer++;
				setChanged();
			}
			while(stepPointer != history.size() - 1 && history.get(stepPointer).step < step);
		}
		else if(step < history.get(stepPointer).step) {
			do {
				stepPointer--;
				setChanged();
			}
			while(stepPointer != -1 && history.get(stepPointer).step > step);
		}
		notifyObservers();
	}
	
	
	
	@Override
	public T getContent() {
		return history.get(stepPointer).value;
	}
	
	public String getCurrentValue() {
		return history.get(stepPointer).value.toString();
	}
	
	public List<T> getHistory() {
		List<T> hist = new ArrayList<>();
		for(StepValue sv : history)
			hist.add(sv.value);
		return hist;
	}
	

	public void setOutOfScope() {
		outOfScope = true;
		setChanged();
		notifyObservers();
	}

	public boolean isOutOfScope() {
		return outOfScope || stepPointer == -1;
	}

}
