package pt.iscte.pandionj.tests.mock;

import java.util.List;

import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.extensibility.Direction;
import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.model.DisplayUpdateObservable;
import pt.iscte.pandionj.parser.VariableInfo;

public class MockArrayIndex
extends DisplayUpdateObservable<Object>
implements IArrayIndexModel {
	
	private final IValueModel variable;
	private final IReferenceModel arrayReference;
	private final Direction direction;
	
	private IBound bound;
	
	public MockArrayIndex(IValueModel variable, IReferenceModel arrayReference, Direction direction) {
		this.variable = variable;
		this.arrayReference = arrayReference;
		this.direction = direction;
		variable.registerObserver((o,a) -> {setChanged(); notifyObservers();});
	}

	public MockArrayIndex(IValueModel variable, IReferenceModel arrayReference, Direction direction, IBound bound) {
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

//	@Override
//	public boolean isWithinScope() {
//		return variable.isWithinScope();
//	}

	@Override
	public VariableInfo getVariableRole() {
		return variable.getVariableRole();
	}

	@Override
	public Role getRole() {
		return Role.ARRAY_ITERATOR;
	}

	@Override
	public boolean isStatic() {
		return variable.isStatic();
	}

	@Override
	public void setOutOfScope() {
		
	}

	@Override
	public boolean update(int step) {
		return false;
	}

	@Override
	public IJavaVariable getJavaVariable() {
		return null;
	}

	@Override
	public void setStep(int stepPointer) {
		
	}
}
