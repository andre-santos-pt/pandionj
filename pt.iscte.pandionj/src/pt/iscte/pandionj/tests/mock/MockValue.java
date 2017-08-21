package pt.iscte.pandionj.tests.mock;


import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.model.DisplayUpdateObservable;
import pt.iscte.pandionj.parser.VariableInfo;

public class MockValue 
extends DisplayUpdateObservable<Object>
implements IValueModel {

	final String type;
	final String name;
	final Role role;
	Object value;
	final boolean isStatic;

	public MockValue(String type, String name, Role role, Object value, boolean isStatic) {
		this.type = type;
		this.name = name;
		this.role = role;
		this.value = value;
		this.isStatic = isStatic;
	}

	@Override
	public String getName() {
		return name;
	}
	@Override
	public String getCurrentValue() {
		return value == null ? "null" : value.toString();
	}
	@Override
	public boolean isDecimal() {
		return type.matches("double|float");
	}
	@Override
	public boolean isBoolean() {
		return type.equals("boolean");
	}
	@Override
	public boolean isInstance() {
		return false;
	}
	@Override
	public String getTypeName() {
		return type;
	}
	@Override
	public List<String> getHistory() {
		return Collections.emptyList();
	}
//	@Override
//	public boolean isWithinScope() {
//		return true;
//	}
	@Override
	public VariableInfo getVariableRole() {
		return null;
	}

	public void set(Object o) {
		value = o;
		setChanged();
		notifyObservers();
	}

	@Override
	public Role getRole() {
		return role;
	}

	@Override
	public boolean isStatic() {
		return isStatic;
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