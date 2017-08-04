package pt.iscte.pandionj.model;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.parser.VariableInfo;

public class ValueModel extends VariableModel<IJavaPrimitiveValue> {
	public enum Role {
		FIXED_VALUE {
			public String toString() { return "Fixed Value";}
		},
		ARRAY_ITERATOR {
			public String toString() { return "Array Index Iterator";}
		},
		FIXED_ARRAY_INDEX {
			public String toString() { return "Fixed Array Index";}
		},
		GATHERER {
			public String toString() { return "Gatherer";}
		},
		STEPPER {
			public String toString() { return "Stepper";}
		},
		MOST_WANTED_HOLDER {
			public String toString() { return "Most-Wanted Holder";}
		},
		NONE {
			public String toString() { return "";}
		};
		
		static Role matchRole(VariableInfo v) {
			if(v == null)											return NONE;
			else if(!v.getArrayFixedVariables().isEmpty())			return FIXED_ARRAY_INDEX;
			else if(v.isFixedValue()) 								return FIXED_VALUE;
			else if(v.isGatherer())									return GATHERER;
			else if(!v.getAccessedArrays().isEmpty())				return ARRAY_ITERATOR;
			else if(v.isStepperBackward() || v.isStepperForward())	return STEPPER;
			else if(v.isMostWantedHolder())							return MOST_WANTED_HOLDER;
			else														return NONE;
		}
		
		public boolean isArrayAccessor() {
			return this == ARRAY_ITERATOR || this == FIXED_ARRAY_INDEX; 
		}
	}
	
	private Role role;
	private VariableInfo var;
	
	public ValueModel(IJavaVariable variable, boolean isInstance, VariableInfo var, StackFrameModel stackFrame) throws DebugException {
		super(variable, isInstance, stackFrame);
		init(var);
	}
	
	public ValueModel(IJavaVariable variable, boolean isInstance, VariableInfo var, RuntimeModel runtime) throws DebugException {
		super(variable, isInstance, runtime);
		assert variable.getValue() instanceof IJavaPrimitiveValue;
		init(var);
	}

	private void init(VariableInfo var) {
		this.var = var;
		role = Role.matchRole(var);
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
	public VariableInfo getVariableRole() {
		return var;
	}

	
	@Override
	public Collection<String> getTags() {
		return Collections.emptyList();
	}
}
