package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.figures.ValueFigure;
import pt.iscte.pandionj.parser.variable.FixedValue;
import pt.iscte.pandionj.parser.variable.Gatherer;
import pt.iscte.pandionj.parser.variable.Variable;

public class ValueModel extends Observable implements ModelElement {
	public enum Role {
		FIXED_VALUE,
		STEPPER,
		GATHERER,
		MOST_WANTED_HOLDER;
		
		
		static Role matchRole(Variable v) {
			if(v instanceof FixedValue)
				return FIXED_VALUE;
			else if(v instanceof Gatherer)
				return GATHERER;
			else
				return null;
		}
	}
	
	private IJavaVariable variable;
	private List<IValue> history;
	private Role role;
	
	public ValueModel(IJavaVariable variable, StackFrameModel model) throws DebugException {
//		assert variable.getValue() instanceof IJavaPrimitiveValue;
		this.variable = variable;
		history = new ArrayList<>();
		history.add(variable.getValue());
		Variable var = model.getLocalVariable(variable.getName());
		role = Role.matchRole(var);
			
		
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
	public IJavaValue getContent() {
		try {
			return (IJavaValue) variable.getValue();
		}
		catch(DebugException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public IFigure createFigure(Graph graph) {
		return new ValueFigure(this, role);
	}
	
	@Override
	public void registerObserver(Observer o) {
		addObserver(o);
	}
}
