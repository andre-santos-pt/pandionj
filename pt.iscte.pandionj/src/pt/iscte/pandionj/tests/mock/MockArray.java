package pt.iscte.pandionj.tests.mock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IRuntimeModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.model.DisplayUpdateObservable;

public class MockArray
extends DisplayUpdateObservable<Object>
implements IArrayModel {

	final String type;
	final List<MockValue> values;
	final List<MockArrayIndex> variableRoles;

	public MockArray(String type, Object ... values) {
		this.type = type;
		this.values = new ArrayList<>();
		for(int i = 0; i < values.length; i++) {
			MockValue var = new MockValue(type, null, null, values[i], false);
			this.values.add(var);
		}
		variableRoles = new ArrayList<>();
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public boolean isMatrix() {
		return false;
	}
	@Override
	public Dimension getMatrixDimension() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean isDecimal() {
		return type.matches("double|float");
	}

	@Override
	public Object getValues() {
		return values.toArray();
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
	public Iterator getValidModelIndexes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List getModelElements() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValidModelIndex(int i) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IRuntimeModel getRuntimeModel() {
		// TODO Auto-generated method stub
		return null;
	}

}