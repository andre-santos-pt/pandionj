package pt.iscte.pandionj.tests.mock;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.model.DisplayUpdateObservable;
import pt.iscte.pandionj.parser.variable.Variable;

public class MockVariable extends DisplayUpdateObservable implements IVariableModel {
	final String type;
	final String name;
	final Variable role;
	final List<String> tags;
	Object value;


	public MockVariable(String type, String name, Variable role, Object value, String ... tags) {
		this.type = type;
		this.name = name;
		this.role = role;
		this.value = value;
		this.tags = new ArrayList<>();
		for(String t : tags)
			this.tags.add(t);
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
	@Override
	public boolean isWithinScope() {
		return true;
	}
	@Override
	public Variable getVariableRole() {
		return role;
	}
	@Override
	public Collection<String> getTags() {
		return Collections.unmodifiableCollection(tags);
	}

	public void set(Object o) {
		value = o;
		setChanged();
		notifyObservers();
	}
}