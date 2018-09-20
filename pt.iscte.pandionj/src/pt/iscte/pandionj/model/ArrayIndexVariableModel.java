package pt.iscte.pandionj.model;

import java.util.List;

import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.extensibility.Direction;
import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IRuntimeModel;
import pt.iscte.pandionj.extensibility.ITag;
import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.parser.VariableInfo;

public class ArrayIndexVariableModel
extends DisplayUpdateObservable<Object> 
implements IArrayIndexModel {

	private final IValueModel model;
	private final IReferenceModel arrayRef;
	
	private ArrayIndexBound bound;
	
	private boolean illegalAccess;
	
	public ArrayIndexVariableModel(IValueModel model, IReferenceModel arrayRef) {
		assert model != null;
		this.model = model;
		this.arrayRef = arrayRef;
		bound = null;
		illegalAccess = false;
		model.registerObserver((a) -> fireChange());
	}
			
	private void fireChange() {
		setChanged();
		notifyObservers();
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public String getName() {
		return model.getName();
	}
	
	public int getCurrentIndex() {
		return Integer.parseInt(model.getCurrentValue());
	}
	
	public boolean isOutOfBounds(IArrayModel array) {
		int i = getCurrentIndex();
		return i < 0 || i >= array.getLength();
	}

//	public boolean isBounded() {
//		return bound != null;
//	}

	public IBound getBound() {
		return bound;
	}
	
	public void setBound(ArrayIndexBound bound) {
		this.bound = bound;
		bound.registerObserver((a) -> fireChange());
	}
	
	public Direction getDirection() {
		if(getVariableRole().isStepperForward())
			return Direction.FORWARD;
		else if(getVariableRole().isStepperBackward())
			return Direction.BACKWARD;
		else
			return Direction.NONE;
	}
	
	void setIllegalAccess() {
		illegalAccess = true;
	}

	public boolean isIllegalAccess() {
		return illegalAccess;
	}

	@Override
	public String getTypeName() {
		return model.getTypeName();
	}

	@Override
	public String getCurrentValue() {
		return model.getCurrentValue();
	}

	@Override
	public List<String> getHistory() {
		return model.getHistory();
	}

	@Override
	public boolean isDecimal() {
		return model.isDecimal();
	}

	@Override
	public boolean isBoolean() {
		return model.isBoolean();
	}

	@Override
	public boolean isInstance() {
		return model.isInstance();
	}

//	@Override
//	public boolean isWithinScope() {
//		return model.isWithinScope();
//	}

	@Override
	public VariableInfo getVariableRole() {
		return model.getVariableRole();
	}

	@Override
	public IVariableModel getArrayReference() {
		return arrayRef;
	}

	@Override
	public Role getRole() {
		return Role.ARRAY_ITERATOR;
	}

	@Override
	public boolean isStatic() {
		return model.isStatic();
	}

	@Override
	public void setOutOfScope() {
		model.setOutOfScope();
	}

	@Override
	public boolean update(int step) {
		return model.update(step);
	}

	@Override
	public IJavaVariable getJavaVariable() {
		return model.getJavaVariable();
	}

	@Override
	public void setVariableRole(VariableInfo info) {
		
	}
	
	@Override
	public IRuntimeModel getRuntimeModel() {
		return model.getRuntimeModel();
	}

	@Override
	public boolean isVisible() {
		return model.isVisible();
	}

	@Override
	public ITag getTag() {
		return model.getTag();
	}

	@Override
	public void setTag(ITag tag) {
		model.setTag(tag);
	}
}