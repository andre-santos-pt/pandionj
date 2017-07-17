package pt.iscte.pandionj.tests.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.model.DisplayUpdateObservable;

public class MockArray extends DisplayUpdateObservable implements IArrayModel {
	final String name;
	final String type;
	final List<MockVariable> values;
	final List<MockArrayIndex> variableRoles;

	public MockArray(String name, String type, Object ... values) {
		this.name = name;
		this.type = type;
		this.values = new ArrayList<>();
		for(int i = 0; i < values.length; i++) {
			MockVariable var = new MockVariable(type, null, null, values[i]);
			this.values.add(var);
		}
		variableRoles = new ArrayList<>();
	}

	@Override
	public boolean isMatrix() {
		return false;
	}

	@Override
	public boolean isDecimal() {
		return type.matches("double|float");
	}

	@Override
	public Object[] getValues() {
		return null;
	}

	@Override
	public int getLength() {
		return values.size();
	}

	@Override
	public int getDimensions() {
		return 1;
	}

	@Override
	public String getComponentType() {
		return type;
	}

	@Override
	public IVariableModel getElementModel(int index) {
		return values.get(index);
	}

	public void addIndexVariable(MockArrayIndex role) {
		variableRoles.add(role);
	}

	public void set(int index, Object value) {
		if(index < 0 || index >= getLength()) {
			setChanged();
			notifyObservers(new IndexOutOfBoundsException());
		}
		else {
			values.get(index).set(value);
			setChanged();
			notifyObservers(index);
		}
	}

	@Override
	public Collection<IArrayIndexModel> getIndexModels() {
		return Collections.unmodifiableList(variableRoles);
	}
	
	public String getName() {
		return name;
	}
}