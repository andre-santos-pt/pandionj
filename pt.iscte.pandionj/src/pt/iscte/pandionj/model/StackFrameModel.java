package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIReturnValueVariable;

import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.ITag;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.parser.ParserManager;
import pt.iscte.pandionj.parser.VarParser;
import pt.iscte.pandionj.parser.VariableInfo;


//TODO debug Exception
public class StackFrameModel extends DisplayUpdateObservable<IStackFrameModel.StackEvent<?>> implements IStackFrameModel {
	private RuntimeModel runtime;
	private IJavaStackFrame frame;
	private Map<String, IVariableModel> stackVars;

	private IFile srcFile;
	private VarParser varParser;
	private IJavaProject javaProject;

	private String invExpression;

	private boolean obsolete;
	private String returnValue;

	private String exceptionType;

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

	public boolean isUserFrame() {
		return javaProject != null && javaProject.exists();
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

	public boolean isInstance() {
		try {
			return !frame.isStatic();
		} catch (DebugException e) {
			return false;
		}

	}


	public void update() throws DebugException {
		handleVariables();
		try {
			lastLine = frame.getLineNumber();
		} catch (DebugException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers(new IStackFrameModel.StackEvent<Object>(StackEvent.Type.STEP, null));
	}



	public void setObsolete() {
		obsolete = true;
		setChanged();
		notifyObservers();
	}

	private void handleVariables() throws DebugException {
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

	private void handleOutOfScopeVars() throws DebugException {
		Iterator<Entry<String, IVariableModel>> iterator = stackVars.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, IVariableModel> e = iterator.next();
			String varName = e.getKey();
			boolean contains = false;
			for(IVariable v : frame.getVariables()) {
				if(v.getName().equals(varName))
					contains = true;
				//				else if(v.getName().equals("this")) {
				//					for (IVariable iv : v.getValue().getVariables())
				//						if(iv.getName().equals(varName))
				//							contains = true;
				//				}
			}
			if(!contains) {
				e.getValue().setOutOfScope();
				iterator.remove();
				setChanged();
				notifyObservers(new StackEvent<IVariableModel>(StackEvent.Type.VARIABLE_OUT_OF_SCOPE, e.getValue()));
			}
		}
	}

	public static boolean isException(JDIReturnValueVariable var) throws DebugException {
		if(var.hasResult) {
			IJavaValue retVal = (IJavaValue) var.getValue();
			if(retVal instanceof IJavaObject) {
				IJavaObject retObj = (IJavaObject) retVal;
				IJavaType javaType = retObj.getJavaType();
				String typeName = javaType.getName();
				try {
					Class<?> c = Class.forName(typeName);
					if(Exception.class.isAssignableFrom(c))
						return true;
				}
				catch(ClassNotFoundException e) {
					return false;
				}
			}
		}
		return false;
	}

	private void handleVar(IJavaVariable jv, boolean isInstance) throws DebugException {
		if(jv instanceof JDIReturnValueVariable) {
			JDIReturnValueVariable retvar = (JDIReturnValueVariable) jv;
			if(retvar.hasResult && !isException(retvar)) {
				IJavaValue retVal = (IJavaValue) jv.getValue();
				runtime.setReturnOnFrame(this, retVal);
			}
			return;
		}

		String varName = jv.getName();
		if(isInstance)
			varName = "this." + varName;

		IJavaValue value = (IJavaValue) jv.getValue();
		if(jv.isStatic()) {
			if(!jv.isPrivate() && !staticRefs.existsVar(this, varName)) { 
				IVariableModel newVar = createVar(jv, false, value);
				staticRefs.add(this, newVar);
			}
		}
		else {
			if(stackVars.containsKey(varName) && stackVars.get(varName).getJavaVariable() == jv) {
				IVariableModel vModel = stackVars.get(varName);
				vModel.update(0);
			}
			else {
				IVariableModel newVar = createVar(jv, isInstance, value);
				stackVars.put(varName, newVar);

				setChanged();
				notifyObservers(new StackEvent<IVariableModel>(StackEvent.Type.NEW_VARIABLE, newVar));
			}
		}
	}

	public VariableInfo getVariableInfo(String varName, boolean isField) {
		try {
			return varParser != null ? varParser.locateVariable(varName, frame.getLineNumber(), isField) : null;
		} catch (DebugException e) {
			return null;
		}
	}

	private IVariableModel createVar(IJavaVariable jv, boolean isInstance, IJavaValue value)
			throws DebugException {
		String varName = jv.getName();
		boolean isField = !jv.isLocal();
		VariableInfo info = getVariableInfo(varName, isField);
		IVariableModel newVar = value instanceof IJavaObject ?
				new ReferenceModel(jv, isInstance, true, info, this) :
					new ValueModel(jv, isInstance, true, info, this);

				if(srcFile != null) {
					ITag tag = ParserManager.getTag(srcFile, jv.getName(), frame.getLineNumber(), isField);
					newVar.setTag(tag);
				}
				return newVar;
	}


	public List<IReferenceModel> getReferencesTo(IEntityModel object) {
		List<IReferenceModel> refs = new ArrayList<>(3);
		findReferences(stackVars, object, refs);
		return refs;
	}


	private static void findReferences(Map<String, IVariableModel> map, IEntityModel object, List<IReferenceModel> refs) {
		for (IVariableModel e : map.values()) {
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
			else if(frame.isConstructor()) {
				return superType() + "new" +  "(" + String.join(", ", args) + ")"; //toSimpleName(frame.getReferenceType().getName())
			}
			else {
				String ret = superType() + frame.getMethodName() + "(" + String.join(", ", args) + ")";
				if(returnValue != null)
					ret += " = " + returnValue;
				return ret;
			}
		} catch (DebugException e) {
			//			e.printStackTrace();
			return super.toString();
		}
	}

	private String superType() throws DebugException {
		String superType = "";
		if(frame.getThis() != null && !frame.getDeclaringTypeName().equals(frame.getThis().getJavaType().getName()))
			superType = toSimpleName(frame.getDeclaringTypeName()) + ".";
		return superType;
	}

	private static  String toSimpleName(String name) {
		String simple = name;
		if(simple.indexOf('.') != -1)
			simple = simple.substring(simple.lastIndexOf('.')+1);

		if(simple.indexOf('$') != -1)
			simple = simple.substring(simple.lastIndexOf('$')+1);

		return simple;
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
				return "[" + s + "]";
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

			if(value instanceof IJavaObject) {
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
		notifyObservers(new StackEvent<String>(StackEvent.Type.RETURN_VALUE, this.returnValue));
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
			return super.toString();
	}

	private StackEvent<String> exceptionEvent;

	public void processException(String exceptionType, int line, String message) {
		this.exceptionType = exceptionType;
		StackEvent.Type type = exceptionType.equals(ArrayIndexOutOfBoundsException.class.getName()) ? 
				StackEvent.Type.ARRAY_INDEX_EXCEPTION : StackEvent.Type.EXCEPTION;
		String arg = type == StackEvent.Type.ARRAY_INDEX_EXCEPTION ? message : exceptionType;
		exceptionEvent = new StackEvent<String>(type, arg);
		setChanged();
		notifyObservers(exceptionEvent);
	}

	public IJavaProject getJavaProject() {
		return javaProject;
	}


	public boolean isObsolete() {
		return obsolete;
	}

	public StackEvent<String> getExceptionEvent() {
		return exceptionEvent;
	}

	public boolean exceptionOccurred() {
		return exceptionType != null;
	}

	public String getExceptionType() {
		return exceptionType;
	}

	public IVariableModel getStackVariable(String varName) {
		IVariableModel var = stackVars.get(varName);
		if(var == null)
			var = stackVars.get("this." + varName);
		return var;
	}

	@Override
	public Collection<IVariableModel> getAllVariables() {
		return Collections.unmodifiableCollection(stackVars.values());
	}

	public Iterable<IVariableModel> getLocalVariables() {
		return stackVars.values().stream()
				.filter(v -> !v.isInstance())
				.collect(Collectors.toList());
	}


	public Iterable<IReferenceModel> getReferenceVariables() {
		return stackVars.values().stream()
				.filter(v -> v instanceof IReferenceModel)
				.map(v -> (IReferenceModel) v)
				.collect(Collectors.toList());
	}

	public boolean isInstanceFrameOf(IObjectModel model) {
		try {
			IJavaObject obj = frame.getThis();
			return obj != null && obj.getUniqueId() == ((ObjectModel) model).getContent().getUniqueId();
		} catch (DebugException e) {
			return false;
		}
	}

}