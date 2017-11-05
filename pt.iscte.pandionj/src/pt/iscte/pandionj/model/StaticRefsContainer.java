package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugException;

import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.IVariableModel;

public class StaticRefsContainer extends DisplayUpdateObservable<IStackFrameModel.StackEvent<?>> implements IStackFrameModel {
	// la.la.Class.VAR -> 
	private Map<String, IVariableModel<?>> map;
	private RuntimeModel runtime;

	public StaticRefsContainer(RuntimeModel runtime) {
		this.runtime = runtime;
		map = new HashMap<>();
	}

	public boolean existsVar(StackFrameModel stackFrameModel, String varName) throws DebugException {
		return get(stackFrameModel, varName) != null;
	}

	public void add(StackFrameModel frame, IVariableModel<?> v) throws DebugException  {
		String id = frame.getStackFrame().getDeclaringTypeName() + "." + v.getName();
		map.put(id,v);
		setChanged();
		notifyObservers(new StackEvent<IVariableModel<?>>(StackEvent.Type.NEW_VARIABLE,v));
	}

	public IVariableModel<?> get(StackFrameModel frame, String varName) throws DebugException {
		String id = frame.getStackFrame().getDeclaringTypeName() + "." + varName;
		return map.get(id);
	}

	@Override
	public Collection<IVariableModel<?>> getStackVariables() {
		if(runtime.isEmpty())
			return Collections.emptyList();

		StackFrameModel topFrame = runtime.getTopFrame();
		try {
			String type = topFrame.getStackFrame().getDeclaringTypeName();
			return map.entrySet().stream()
					.filter((e) -> e.getKey().substring(0, e.getKey().lastIndexOf('.')).equals(type))
					.map((e) -> e.getValue())
					.collect(Collectors.toList());
		} catch (DebugException e1) {
			e1.printStackTrace();
		}
		return Collections.emptyList();
	}

	@Override
	public Collection<IReferenceModel> getReferencesTo(IEntityModel e) {
		Collection<IReferenceModel> refs = new ArrayList<>();
		for(IVariableModel<?> v : map.values())
			if(v instanceof IReferenceModel && ((IReferenceModel) v).getModelTarget() == e)
				refs.add((IReferenceModel) v);

		return refs;
	}

	@Override
	public RuntimeModel getRuntime() {
		return runtime;
	}

	@Override
	public String getInvocationExpression() {
		return "(static frame)";
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


}
