package pt.iscte.pandionj.tests.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;

import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.model.RuntimeModel;
import pt.iscte.pandionj.parser.VariableInfo;

public class MockStackFrame implements IStackFrameModel {

	private List<IVariableModel> elements = new ArrayList<>();

	public void add(IVariableModel e) {
		elements.add(e);
	}
	
	@Override
	public Collection<IVariableModel> getAllVariables() {
		return Collections.unmodifiableCollection(elements);
	}

	@Override
	public Collection<IReferenceModel> getReferenceVariables() {
		return elements.stream()
				.filter((v) -> v instanceof IReferenceModel)
				.map((e) -> (IReferenceModel) e)
				.collect(Collectors.toList());
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
	public String getInvocationExpression() {
		return null;
	}

	@Override
	public boolean isObsolete() {
		return false;
	}

	@Override
	public boolean isExecutionFrame() {
		return false;
	}

	@Override
	public int getLineNumber() {
		return 0;
	}
	
	@Override
	public boolean exceptionOccurred() {
		return false;
	}
	@Override
	public IFile getSourceFile() {
		return null;
	}

	@Override
	public String getExceptionType() {
		return null;
	}

	@Override
	public boolean isInstance() {
		return false;
	}

	@Override
	public boolean isInstanceFrameOf(IObjectModel model) {
		return false;
	}

	@Override
	public StackEvent<String> getExceptionEvent() {
		return null;
	}

	@Override
	public Collection<IVariableModel> getLocalVariables() {
		return null;
	}

	@Override
	public VariableInfo getVariableInfo(String varName, boolean isField) {
		return null;
	}	
}
