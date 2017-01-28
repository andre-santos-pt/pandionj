package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.figures.ValueFigure;

public class PrimitiveVariableModel extends Observable implements ModelElement {

	private IJavaVariable variable;
	private List<IValue> history;

	public PrimitiveVariableModel(IJavaVariable variable) throws DebugException {
		assert variable.getValue() instanceof IJavaPrimitiveValue;
		this.variable = variable;
		history = new ArrayList<>();
		history.add(variable.getValue());
	}

	public String getName() {
		try {
			return variable.getName();
		} catch (DebugException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getCurrentValue() {
		return history.get(history.size()-1).toString();
	}

	public List<IValue> getAllValues() {
		return Collections.unmodifiableList(history);
	}
	
	@Override
	public void update() {
		try {
			boolean equals = variable.getValue().equals(history.get(history.size()-1));
			if(!equals) {
				history.add(variable.getValue());
				setChanged();
				notifyObservers();
			}
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
	}

	@Override
	public IJavaPrimitiveValue getContent() {
		try {
			return (IJavaPrimitiveValue) variable.getValue();
		}
		catch(DebugException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public IFigure createFigure() {
		return new ValueFigure(this);
	}

}
