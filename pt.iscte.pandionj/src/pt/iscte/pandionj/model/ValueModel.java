package pt.iscte.pandionj.model;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

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
			if(v instanceof FixedValue) 				return FIXED_VALUE;
			else if(v instanceof Gatherer)			return GATHERER;
			else if(v instanceof ArrayIterator)		return ARRAY_ITERATOR;
			else if(v instanceof MostWantedHolder)	return MOST_WANTED_HOLDER;
			else										return NONE;
		}
	}
	
	private Role role;
	private Variable var;
	
	public ValueModel(IJavaVariable variable, boolean isInstance, StackFrameModel stackFrame, Variable var) throws DebugException {
		super(variable, isInstance, stackFrame);
		init(var);
	}
	
	public ValueModel(IJavaVariable variable, boolean isInstance, RuntimeModel runtime, Variable var) throws DebugException {
		super(variable, isInstance, runtime);
		assert variable.getValue() instanceof IJavaPrimitiveValue;
		init(var);
	}

	private void init(Variable var) {
		this.var = var;
		role = Role.matchRole(var);
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
		return getTypeName().matches("float|double"); // TODO Float/Double??
	}

	public boolean isBoolean() {
		return getTypeName().equals("boolean"); // TODO Boolean?
	}
	
	public Role getRole() {
		return role;
	}

	@Override
	public Variable getVariableRole() {
		return var;
	}

	
	@Override
	public Collection<String> getTags() {
		return Collections.emptyList();
	}
}
