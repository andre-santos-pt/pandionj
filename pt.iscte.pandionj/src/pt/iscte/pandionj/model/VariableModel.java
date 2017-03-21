package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

public abstract class VariableModel<T extends IJavaValue> extends ModelElement<T> {

	protected IJavaVariable variable;
	private String name;
	private List<T> history;
	
	public VariableModel(IJavaVariable variable, StackFrameModel model) {
		super(model);
		assert variable != null && model != null;
		this.variable = variable;
		try {
			this.name = variable.getName();
		} catch (DebugException e) {
			e.printStackTrace();
		}
		history = new ArrayList<>();
		history.add(getContent());
	}
	
	public String getName() {		
		return name;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T getContent() {
		try {
			return (T) variable.getValue();
		}
		catch(DebugException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void update() {
		try {
			boolean equals = variable.getValue().equals(history.get(history.size()-1));
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
	
	// TODO: remove?
	public String getCurrentValue() {
		return history.get(history.size()-1).toString();
	}

}
