package pt.iscte.pandionj.tests.mock;

import java.util.Collection;
import java.util.List;

import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.model.DisplayUpdateObservable;
import pt.iscte.pandionj.parser.VariableInfo;

public class MockArrayIndex extends DisplayUpdateObservable implements IArrayIndexModel {
	private final IVariableModel variable;
	private final IVariableModel arrayReference;
	private final Direction direction;
	
	private IBound bound;
	
	public MockArrayIndex(IVariableModel variable, IVariableModel arrayReference, Direction direction) {
		this.variable = variable;
		this.arrayReference = arrayReference;
		this.direction = direction;
		variable.registerObserver((o,a) -> {setChanged(); notifyObservers();});
	}

	public MockArrayIndex(IVariableModel variable, IVariableModel arrayReference, Direction direction, IBound bound) {
		this(variable, arrayReference, direction);
		this.bound = bound;
	}
	
	@Override
	public int getCurrentIndex() {
		return Integer.parseInt(getCurrentValue());
	}

	@Override
	public Direction getDirection() {
		return direction;
	}

	@Override
	public IBound getBound() {
		return bound;
	}

	@Override
	public IVariableModel getArrayReference() {
		return arrayReference;
	}

	@Override
	public String getName() {
		return variable.getName();
	}

	@Override
	public String getTypeName() {
		return variable.getTypeName();
	}

	@Override
	public String getCurrentValue() {
		return variable.getCurrentValue();
	}

	@Override
	public List<String> getHistory() {
		return variable.getHistory();
	}

	@Override
	public boolean isDecimal() {
		return variable.isDecimal();
	}

	@Override
	public boolean isBoolean() {
		return variable.isBoolean();
	}

	@Override
	public boolean isInstance() {
		return variable.isInstance();
	}

	@Override
	public boolean isWithinScope() {
		return variable.isWithinScope();
	}

	@Override
	public VariableInfo getVariableRole() {
		return variable.getVariableRole();
	}

	@Override
	public Collection<String> getTags() {
		return variable.getTags();
	}
}
