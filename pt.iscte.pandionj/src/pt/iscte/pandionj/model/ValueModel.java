package pt.iscte.pandionj.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.parser.VariableInfo;

public class ValueModel
extends VariableModel<IJavaPrimitiveValue, Object> 
implements IValueModel {
	
	private Role role;
	private VariableInfo info;
	
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
		this.info = var;
		role = matchRole(var);
	}
	
	private static Role matchRole(VariableInfo v) {
		if(v == null)											return Role.NONE;
		else if(!v.getArrayFixedVariables().isEmpty())			return Role.FIXED_ARRAY_INDEX;
		else if(v.isFixedValue()) 								return Role.FIXED_VALUE;
		else if(v.isGatherer())									return Role.GATHERER;
		else if(!v.getAccessedArrays().isEmpty())				return Role.ARRAY_ITERATOR;
		else if(v.isStepperBackward() || v.isStepperForward())	return Role.STEPPER;
		else if(v.isMostWantedHolder())							return Role.MOST_WANTED_HOLDER;
		else														return Role.NONE;
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
		return getTypeName().matches("float|double");
	}

	public boolean isBoolean() {
		return getTypeName().equals("boolean");
	}
	
	public Role getRole() {
		return role;
	}

	@Override
	public VariableInfo getVariableRole() {
		return info;
	}

	@Override
	public void setVariableRole(VariableInfo info) {
		this.info = info;		
	}
}
