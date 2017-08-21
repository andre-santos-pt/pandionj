package pt.iscte.pandionj.tests.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.model.RuntimeModel;

public class MockStackFrame implements IStackFrameModel {

	private List<IVariableModel> elements = new ArrayList<>();

	public void add(IVariableModel e) {
		elements.add(e);
	}
	
	@Override
	public Collection<IVariableModel> getStackVariables() {
		return Collections.unmodifiableCollection(elements);
	}

	@Override
	public Collection<IReferenceModel> getReferencesTo(IEntityModel e) {
		return Collections.emptyList();
	}

	@Override
	public RuntimeModel getRuntime() {
		return null;
	}

	@Override
	public Collection<IVariableModel> getAllVariables() {
		return getStackVariables();
	}

	
	
}
