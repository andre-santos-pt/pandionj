package pt.iscte.pandionj.model;

import java.util.Collection;
import java.util.List;

import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.parser.VariableInfo;

public class ArrayIndexVariableModel extends DisplayUpdateObservable implements IArrayIndexModel {
	private final IVariableModel model;
	private final IVariableModel arrayRef;
	
	private IBound bound;
	
	private boolean illegalAccess;
	
	public ArrayIndexVariableModel(IVariableModel model, IVariableModel arrayRef) {
		assert model != null;
		this.model = model;
		this.arrayRef = arrayRef;
		bound = null;
		illegalAccess = false;
		model.registerObserver((o,a) -> {setChanged(); notifyObservers();});
	}
			
	public ArrayIndexVariableModel(IVariableModel model, IVariableModel arrayRefName, IBound bound) {
		this(model, arrayRefName);
		this.bound = bound;
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

	public boolean isBounded() {
		return bound != null;
	}

	public IBound getBound() {
		return bound;
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

	@Override
	public boolean isWithinScope() {
		return model.isWithinScope();
	}

	@Override
	public VariableInfo getVariableRole() {
		return model.getVariableRole();
	}

	@Override
	public Collection<String> getTags() {
		return model.getTags();
	}

	@Override
	public IVariableModel getArrayReference() {
		return arrayRef;
	}
}