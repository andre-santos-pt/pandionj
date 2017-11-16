package pt.iscte.pandionj.tests.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;

import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.model.RuntimeModel;

public class MockStackFrame implements IStackFrameModel {

	private List<IVariableModel<?>> elements = new ArrayList<>();

	public void add(IVariableModel<?> e) {
		elements.add(e);
	}
	
	@Override
	public Collection<IVariableModel<?>> getAllVariables() {
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
	public String getInvocationExpression() {
		return null;
	}

	@Override
	public boolean isObsolete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isExecutionFrame() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getLineNumber() {
		return 0;
	}
	
	@Override
	public boolean exceptionOccurred() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public IFile getSourceFile() {
		return null;
	}

	@Override
	public String getExceptionType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isInstance() {
		return false;
	}

	@Override
	public boolean isInstanceFrameOf(IObjectModel model) {
		// TODO Auto-generated method stub
		return false;
	}

	
}
