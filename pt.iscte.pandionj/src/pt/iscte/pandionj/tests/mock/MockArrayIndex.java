package pt.iscte.pandionj.tests.mock;

import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.parser.VariableInfo;

public class MockArrayIndex extends MockVariable implements IArrayIndexModel {
	private final String arrayRefName;
	private final Direction direction;
	
	private Integer constBound;
	private IVariableModel varBound;
	
	public MockArrayIndex(String name, String arrayRefName, VariableInfo role, int value, Direction direction) {
		super("int", name, role, value);
		this.arrayRefName = arrayRefName;
		this.direction = direction;
	}

	public MockArrayIndex(String name, String arrayRefName, VariableInfo role, int value, Direction direction, int constBound) {
		this(name, arrayRefName, role, value, direction);
		this.constBound = constBound;
	}
	
	public MockArrayIndex(String name, String arrayRefName, VariableInfo role, int value, Direction direction, IVariableModel varBound) {
		this(name, arrayRefName, role, value, direction);
		this.varBound = varBound;
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
	public boolean isBounded() {
		return constBound != null || varBound != null;
	}

	@Override
	public int getBound() {
		assert isBounded();
		return constBound != null ? constBound.intValue() : Integer.parseInt(varBound.getCurrentValue());
	}

	@Override
	public String getArrayReferenceName() {
		return arrayRefName;
	}
}
