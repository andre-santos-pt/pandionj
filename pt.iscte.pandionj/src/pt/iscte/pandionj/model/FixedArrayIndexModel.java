package pt.iscte.pandionj.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.parser.VariableInfo;

public class FixedArrayIndexModel implements IArrayIndexModel {
	private final IVariableModel model;

	public FixedArrayIndexModel(IVariableModel model, IVariableModel arrayRef) {
		this.model = model;
		
	}
	
	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getTypeName() {
		return int.class.getName();
	}

	@Override
	public String getCurrentValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getHistory() {
		return Collections.emptyList();
	}

	@Override
	public boolean isDecimal() {
		return false;
	}

	@Override
	public boolean isBoolean() {
		return false;
	}

	@Override
	public boolean isInstance() {
		return false;
	}

	@Override
	public boolean isWithinScope() {
		return false;
	}

	@Override
	public VariableInfo getVariableRole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getTags() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVariableModel getArrayReference() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCurrentIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Direction getDirection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IBound getBound() {
		// TODO Auto-generated method stub
		return null;
	}

}
