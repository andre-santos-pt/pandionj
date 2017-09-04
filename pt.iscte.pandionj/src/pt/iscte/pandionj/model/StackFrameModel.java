package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.ParserManager;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.parser.VarParser;
import pt.iscte.pandionj.parser.VariableInfo;



public class StackFrameModel extends DisplayUpdateObservable<IStackFrameModel.StackEvent<?>> implements IStackFrameModel {
	private RuntimeModel runtime;
	private IJavaStackFrame frame;
	private Map<String, IVariableModel<?>> stackVars;

	private IFile srcFile;
	private VarParser varParser;
	private IJavaProject javaProject;

	private String invExpression;

	private boolean obsolete;
	private String returnValue;
	private String exceptionType;

	private int step;
	private int stepPointer;
	private Map<Integer, Integer> stepLines;

	private int lastLine;

	private StaticRefsContainer staticRefs;

	public StackFrameModel(RuntimeModel runtime, IJavaStackFrame frame, StaticRefsContainer staticRefs) {
		assert runtime != null && frame != null && staticRefs != null;
		this.runtime = runtime;
		this.frame = frame;
		this.staticRefs = staticRefs;

		stackVars = new LinkedHashMap<>();

		Object sourceElement = frame.getLaunch().getSourceLocator().getSourceElement(frame);
		if(sourceElement instanceof IFile) {
			srcFile = (IFile) frame.getLaunch().getSourceLocator().getSourceElement(frame);
			varParser = ParserManager.getVarParserResult(srcFile);
			javaProject = JavaCore.create(srcFile.getProject());
		}

		obsolete = false;
		exceptionType = null;

		step = 0;
		stepLines = new HashMap<>();
		try {
			lastLine = frame.getLineNumber();
			stepLines.put(step, lastLine);
		} catch (DebugException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return getInvocationExpression();
	}
	
	public RuntimeModel getRuntime() {
		return runtime;
	}

	public boolean isExecutionFrame() {
		return !runtime.isEmpty() && runtime.getTopFrame() == this;
	}

	public IJavaStackFrame getStackFrame() {
		return frame;
	}

	public IFile getSourceFile() {
		return srcFile;
	}

	public int getLineNumber() {
		try {
			return frame.getLineNumber();
		} catch (DebugException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public void update() {
		handleVariables(); // FIXME bug new variables int[][]
		//		if(hasChanged()) {
		step++;
		stepPointer = step;
		stepLines.put(step, lastLine);

		try {
			lastLine = frame.getLineNumber();
		} catch (DebugException e) {
			e.printStackTrace();
		}
	}



	public void setObsolete() {
		obsolete = true;
		setChanged();
		notifyObservers();
	}

	private void handleVariables() {
		try {
			handleOutOfScopeVars();

			for(IVariable v : frame.getVariables()) {
				IJavaVariable jv = (IJavaVariable) v;
				String varName = v.getName();
				if(varName.equals("this")) {
					for (IVariable iv : jv.getValue().getVariables()) {
						IJavaVariable att = (IJavaVariable) iv;
						if(!att.isSynthetic() && !att.isStatic()) {
							handleVar(att, true);
						}
					}
				}

				else if(!jv.isSynthetic()) {
					handleVar(jv, false);
				}
			}
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
	}

	private void handleOutOfScopeVars() throws DebugException {
		Iterator<Entry<String, IVariableModel<?>>> iterator = stackVars.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, IVariableModel<?>> e = iterator.next();
			String varName = e.getKey();
			boolean contains = false;
			for(IVariable v : frame.getVariables()) {
				if(v.getName().equals(varName))
					contains = true;
				else if(v.getName().equals("this")) {
					for (IVariable iv : v.getValue().getVariables())
						if(iv.getName().equals(varName))
							contains = true;
				}
			}
			if(!contains) {
				e.getValue().setOutOfScope();
				iterator.remove();
				setChanged();
				notifyObservers(new StackEvent<IVariableModel<?>>(StackEvent.Type.VARIABLE_OUT_OF_SCOPE, e.getValue()));
			}
		}
	}


	private void handleVar(IJavaVariable jv, boolean isInstance) throws DebugException {
		if(jv.getClass().getSimpleName().equals("JDIReturnValueVariable")) {
			runtime.setReturnOnFrame(this, (IJavaValue) jv.getValue());
			return;
		}

		String varName = jv.getName();
		IJavaValue value = (IJavaValue) jv.getValue();

		if(jv.isStatic()) {
			if(!staticRefs.existsVar(this, varName)) {
				IVariableModel<?> newVar = createVar(jv, false, value);
				staticRefs.add(this, newVar);
			}
		}
		else {
			if(stackVars.containsKey(varName) && stackVars.get(varName).getJavaVariable() == jv) {
				IVariableModel<?> vModel = stackVars.get(varName);
				vModel.update(step);
			}
			else {
				IVariableModel<?> newVar = createVar(jv, isInstance, value);
				stackVars.put(varName, newVar);

				setChanged();
				notifyObservers(new StackEvent<IVariableModel<?>>(StackEvent.Type.NEW_VARIABLE, newVar));
			}
		}
	}

	private IVariableModel<?> createVar(IJavaVariable jv, boolean isInstance, IJavaValue value)
			throws DebugException {
		String varName = jv.getName();
		boolean isField = !jv.isLocal();
		VariableInfo info = varParser != null ? varParser.locateVariable(varName, frame.getLineNumber(), isField) : null;
		System.err.println(frame.getDeclaringTypeName() + " -- " +  frame.getMethodName() + " " + (jv.isStatic() ? "static " : "") + varName + ": " + info);
		IVariableModel<?> newVar = null;

		if(value instanceof IJavaObject) {
			ReferenceModel refElement = new ReferenceModel(jv, isInstance, info, this);
			Collection<String> tags = ParserManager.getTags(srcFile, jv.getName(), frame.getLineNumber(), isField);
			refElement.setTags(tags);
			newVar = refElement;
		}
		else {
			newVar = new ValueModel(jv, isInstance, info, this);
		}
		return newVar;
	}


	public Collection<IReferenceModel> getReferencesTo(IEntityModel object) {
		List<IReferenceModel> refs = new ArrayList<>(3);
		findReferences(stackVars, object, refs);
		return refs;
	}

	private static void findReferences(Map<String, IVariableModel<?>> map, IEntityModel object, List<IReferenceModel> refs) {
		for (IVariableModel<?> e : map.values()) {
			if(e instanceof ReferenceModel && ((ReferenceModel) e).getModelTarget().equals(object))
				refs.add((ReferenceModel) e);
		}
	}


	private String calcString() {
		try {
			IJavaVariable[] localVariables = frame.getLocalVariables();
			int nArgs = frame.getArgumentTypeNames().size();
			List<String> args = new ArrayList<>(localVariables.length);
			for(int i = 0; i < localVariables.length && i < nArgs ; i++) {
				if(PrimitiveType.isPrimitive(localVariables[i].getReferenceTypeName())) {
					IJavaValue value = (IJavaValue) localVariables[i].getValue();
					args.add(valueToString(value));
				}
				else
					args.add(localVariables[i].getName());
			}

			if(frame.isStaticInitializer())
				return frame.getDeclaringTypeName() + " (static initializer)";
			else if(frame.isConstructor())
				return "new " + frame.getReferenceType().getName() + "(" + String.join(", ", args) + ")";
			else {
				String ret = frame.getMethodName() + "(" + String.join(", ", args) + ")";
				if(returnValue != null)
					ret += " = " + returnValue;
				return ret;
			}
		} catch (DebugException e) {
			e.printStackTrace();
			return super.toString();
		}
	}

	private String valueToString(IJavaValue value) {
		if(value instanceof IJavaArray) {
			try {
				IJavaArray array = (IJavaArray) value;
				IJavaValue[] values = array.getValues();
				String s = "";
				for(int i = 0; i < values.length; i++) {
					if(!s.isEmpty())
						s += ", ";
					s += valueSpecialChars(values[i]);
				}
				return "{" + s + "}";
			}
			catch (DebugException e) {
				e.printStackTrace();
				return null;
			}
		}
		else
			return valueSpecialChars(value);
	}

	private String valueSpecialChars(IJavaValue value) {
		try {
			if(value.getReferenceTypeName().equals("char"))
				return "'" + value.getValueString() + "'";

			if(value.getReferenceTypeName().equals(String.class.getName()))
				return "\"" + value.getValueString() + "\"";

			if(value instanceof IJavaObject) { // FIXME bug
				if(((IJavaObject) value).isNull())
					return "null";
				else
					return runtime.getObject((IJavaObject) value, false, null).toString();
			}
			return value.getValueString();
		}
		catch (DebugException e) {
			e.printStackTrace();
			return null;
		}
	}


	public void setReturnValue(IJavaValue v) {
		this.returnValue = valueToString(v);
		obsolete = true;
		setChanged();
		notifyObservers();
	}

	public String getInvocationExpression() {
		if(isObsolete() && returnValue != null && !returnValue.equals("(void)"))
			return invExpression + " = " + returnValue;
		else if(invExpression == null && !isObsolete()) {
			invExpression = calcString();
			return invExpression;
		}
		else if(invExpression != null)
			return invExpression;
		else
			return toString();
	}

	public void processException(String exceptionType, int line) {
		this.exceptionType = exceptionType;

		//		Collection<JavaException> collection = codeAnalysis.lineExceptions.get(line);
		//		for(JavaException e : collection)
		//			if(e instanceof ArrayOutOfBounds) {
		//				ArrayOutOfBounds ae = (ArrayOutOfBounds) e;
		//				ArrayPrimitiveModel arrayModel = (ArrayPrimitiveModel) ((ReferenceModel) vars.get(ae.arrayName)).getModelTarget();
		//				arrayModel.setVarError(ae.arrayAccess);
		//			}

		setChanged();
		notifyObservers(new StackEvent<String>(StackEvent.Type.EXCEPTION, exceptionType));
	}

	public IJavaProject getJavaProject() {
		return javaProject;
	}


	public boolean isObsolete() {
		return obsolete;
	}

	public boolean exceptionOccurred() {
		return exceptionType != null;
	}

	public String getExceptionMessage() {
		return exceptionType;
	}

	public int getRunningStep() {
		return step;
	}

	public int getStepPointer() {
		return stepPointer;
	}

	public void setStep(int step) {
		if(step != stepPointer) {
			stepPointer = step;
			//			for(IVariableModel<?> var : staticVars.values())
			//				var.setStep(stepPointer);

			for(IVariableModel<?> var : stackVars.values())
				var.setStep(stepPointer);
		}
		setChanged();
		notifyObservers();
	}

	public int getStepLine() {
		return stepLines.get(stepPointer);
	}

	public IVariableModel getStackVariable(String varName) {
		return stackVars.get(varName);
	}

	@Override
	public Collection<IVariableModel<?>> getStackVariables() {
		return Collections.unmodifiableCollection(stackVars.values());
	}

	public Collection<IVariableModel<?>> getAllVariables() {
		ArrayList<IVariableModel<?>> vars = new ArrayList<>();
		//		vars.addAll(staticVars.values());
		vars.addAll(stackVars.values());
		return vars;
	}



}