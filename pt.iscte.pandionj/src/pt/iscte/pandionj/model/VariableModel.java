package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

public abstract class VariableModel<T extends IJavaValue> extends ModelElement<T> {
	
	protected IJavaVariable variable;
	private String type;
	private String name;
	private final boolean isInstance;
	private boolean outOfScope;
	private List<T> history;
	
	public VariableModel(IJavaVariable variable, boolean isInstance, StackFrameModel model) {
		super(model);
		assert variable != null;
		
		this.variable = variable;
		this.isInstance = isInstance;
		history = new ArrayList<>();

		try {
			this.type = variable.getReferenceTypeName();
			this.name = variable.getName();
			T val = (T) variable.getValue();
			history.add(val);
			
		} catch (DebugException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void update(int step) {
		try {
			T current = history.get(history.size()-1);
			boolean equals = variable.getValue().equals(current);
			if(!equals) {
				history.add((T) variable.getValue());
				setChanged();
				notifyObservers();
			}
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
	}
	
	public String getName() {		
		return name;
	}
	
	public boolean isInstance() {
		return isInstance;
	}
	
	public String getType() {
//		try {
//			return variable.getReferenceTypeName();
//		} catch (DebugException e) {
//			e.printStackTrace();
//			return null;
//		}
		return type;
	}
//	@SuppressWarnings("unchecked")
	@Override
	public T getContent() {
//		try {
//			return (T) variable.getValue();
//		}
//		catch(DebugException e) {
//			e.printStackTrace();
//			return null;
//		}
		return history.get(history.size()-1);
	}
	
//	public IJavaType getVariableType() {
//		return type;
//	}
	

	
	public String getCurrentValue() {
		return history.get(history.size()-1).toString();
	}
	
	public List<T> getHistory() {
		return Collections.unmodifiableList(history);
	}

	public void setOutOfScope() {
		outOfScope = true;
		setChanged();
		notifyObservers();
	}

	public boolean isOutOfScope() {
		return outOfScope;
	}

}
