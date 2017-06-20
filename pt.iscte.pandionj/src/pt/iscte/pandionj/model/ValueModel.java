package pt.iscte.pandionj.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.figures.ValueFigure;
import pt.iscte.pandionj.parser.variable.FixedValue;
import pt.iscte.pandionj.parser.variable.Gatherer;
import pt.iscte.pandionj.parser.variable.MostWantedHolder;
import pt.iscte.pandionj.parser.variable.Stepper.ArrayIterator;
import pt.iscte.pandionj.parser.variable.Variable;

public class ValueModel extends VariableModel<IJavaPrimitiveValue> {
	public enum Role {
		FIXED_VALUE {
			public String toString() { return "Fixed Value";}
		},
		ARRAY_ITERATOR {
			public String toString() { return "Array Index Iterator";}
		},
		GATHERER {
			public String toString() { return "Gatherer";}
		},
		MOST_WANTED_HOLDER {
			public String toString() { return "Most-Wanted Holder";}
		},
		NONE {
			public String toString() { return "";}
		};
		
		static Role matchRole(Variable v) {
			if(v instanceof FixedValue) 			return FIXED_VALUE;
			else if(v instanceof Gatherer)			return GATHERER;
			else if(v instanceof ArrayIterator)		return ARRAY_ITERATOR;
			else if(v instanceof MostWantedHolder)	return MOST_WANTED_HOLDER;
			else									return NONE;
		}
	}
	
	private Role role;
	private Variable var;
	
	public ValueModel(IJavaVariable variable, boolean isInstance, StackFrameModel model) throws DebugException {
		super(variable, isInstance, model);
		assert variable.getValue() instanceof IJavaPrimitiveValue;
		var = model.getLocalVariable(variable.getName());
		role = Role.matchRole(var);
	}

	@Override
	public IFigure createInnerFigure() {
		return new ValueFigure(this, role);
	}
	
	public Variable getVariable() {
		return var;
	}
	
	@Override
	public String toString() {
		try {
			return getName() + " = " + getContent().getValueString() + (role != Role.NONE ? " (" + role + ")" : "");
		} catch (DebugException e) {
			e.printStackTrace();
			return getClass().getSimpleName();
		}
	}

	public boolean isDecimal() {
		return getType().matches("float|double"); // TODO Float/Double??
	}

	public boolean isBoolean() {
		return getType().equals("boolean"); // TODO Boolean?
	}
	
	public Role getRole() {
		return role;
	}

	
	
}
