package pt.iscte.pandionj.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.figures.ValueFigure;
import pt.iscte.pandionj.parser.variable.FixedValue;
import pt.iscte.pandionj.parser.variable.Gatherer;
import pt.iscte.pandionj.parser.variable.MostWantedHolder;
import pt.iscte.pandionj.parser.variable.Variable;

public class ValueModel extends VariableModel<IJavaPrimitiveValue> {
	public enum Role {
		FIXED_VALUE {
			public String toString() { return "Fixed Value";}
		},
		STEPPER {
			public String toString() { return "Stepper";}
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
	public IFigure createInnerFigure(Graph graph) {
		return new ValueFigure(this, role);
	}
	
	public Variable getVariable() {
		return var;
	}
	
	@Override
	public String toString() {
		try {
			return getName() + " = " + getContent().getValueString() + " (" + role + ")";
		} catch (DebugException e) {
			e.printStackTrace();
			return getClass().getSimpleName();
		}
	}

	public boolean isDecimal() {
		try {
			return getVariableType().getName().matches("float|double");
		} catch (DebugException e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
