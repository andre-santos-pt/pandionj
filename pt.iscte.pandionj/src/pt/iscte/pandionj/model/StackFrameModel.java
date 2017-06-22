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
import java.util.stream.Collectors;

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

import com.google.common.collect.Multimap;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.CharValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;

import pt.iscte.pandionj.ParserManager;
import pt.iscte.pandionj.parser.ParserAPI.ParserResult;
import pt.iscte.pandionj.parser.exception.JavaException;
import pt.iscte.pandionj.parser.exception.JavaException.ArrayOutOfBounds;
import pt.iscte.pandionj.parser.variable.Stepper.ArrayIterator;
import pt.iscte.pandionj.parser.variable.Variable;



@SuppressWarnings("restriction")
public class StackFrameModel extends DisplayUpdateObservable {
	private IJavaStackFrame frame;
	private Map<String, VariableModel<?>> vars;

	private ParserResult codeAnalysis;
	private IFile srcFile;
	private IJavaProject javaProject;

	private String invExpression;

	private boolean obsolete;
	private String exceptionType;

	private RuntimeModel runtime;

	private int step;

	private int stepPointer;
	private Map<Integer, Integer> stepLines;

	private int lastLine;

	private Value returnValue;
	
	//	private StackFrameImpl underlyingFrame;

	public StackFrameModel(RuntimeModel runtime, IJavaStackFrame frame) {
		//		JDIStackFrame jdif = (JDIStackFrame) frame;
		//		try {
		//			Field field = JDIStackFrame.class.getDeclaredField("fStackFrame");
		//			field.setAccessible(true);
		//			underlyingFrame = (StackFrameImpl) field.get(jdif);
		//		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
		//			e.printStackTrace();
		//		} 

		this.runtime = runtime;
		this.frame = frame;
		vars = new LinkedHashMap<>();
		srcFile = (IFile) frame.getLaunch().getSourceLocator().getSourceElement(frame);
		codeAnalysis = ParserManager.getParserResult(srcFile);
		javaProject = JavaCore.create(srcFile.getProject());

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

	public boolean isExecutionFrame() {
		return !runtime.isEmpty() && runtime.getTopFrame() == this;
	}

	//	public boolean matchesFrame(StackFrame uFrame) {
	//		return underlyingFrame.equals(uFrame);
	//	}

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

	public Variable getLocalVariable(String name) {
		try {
			int line = frame.getLineNumber();
			for(Variable v : codeAnalysis.variableRoles.get(name))
				if(v.scopeIncludesLine(line))
					return v;
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void update() {
		List<VariableModel<?>> newVars = handleVariables();

//		for(EntityModel<?> o : objects.values().toArray(new EntityModel[objects.size()])) {
//			if(o instanceof ArrayModel && o.update(step))
//				setChanged();
//			else if(o instanceof ObjectModel)
//				((ObjectModel) o).traverseSiblings(new SiblingVisitor() {
//					public void visit(EntityModel<?> object, ObjectModel parent, int index, int depth, String field) {
//						if(object != null && object.update(step))
//							setChanged();
//					}
//				});
//		}
		
		if(hasChanged()) {
			step++;
			stepPointer = step;
			stepLines.put(step, lastLine);

			try {
				lastLine = frame.getLineNumber();
			} catch (DebugException e) {
				e.printStackTrace();
			}
		}
		notifyObservers(newVars);
	}

	

	public void setObsolete() {
		obsolete = true;
		setChanged();
		notifyObservers(Collections.emptyList());
	}

	private List<VariableModel<?>> handleVariables() {
		List<VariableModel<?>> newVars = new ArrayList<>();
		try {
			// TODO getThis
			Iterator<Entry<String, VariableModel<?>>> iterator = vars.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<String, VariableModel<?>> e = iterator.next();
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
				}
			}

			for(IVariable v : frame.getVariables()) {
				IJavaVariable jv = (IJavaVariable) v;
				String varName = v.getName();

				if(varName.equals("this")) {
					for (IVariable iv : jv.getValue().getVariables())
						if(!((IJavaVariable) iv).isSynthetic() && !((IJavaVariable) iv).isStatic()) {
							VariableModel<?> var = handleVar((IJavaVariable) iv, true);
							if(var != null)
								newVars.add(var);
						}
				}

				else if(!jv.isSynthetic()) {
					VariableModel<?> var = handleVar(jv, false);
					if(var != null)
						newVars.add(var);
				}
			}

			handleArrayIterators();
		}
		catch(DebugException e) {
			e.printStackTrace();
		}

		if(!newVars.isEmpty())
			setChanged();

		return newVars;
	}


	private VariableModel<?> handleVar(IJavaVariable jv, boolean isInstance) throws DebugException {
		String varName = jv.getName();

		IJavaValue value = (IJavaValue) jv.getValue();

		if(vars.containsKey(varName)) {
			VariableModel<?> vModel = vars.get(varName);
			boolean change = vModel.update(step);
			if(change)
				setChanged();
			if(change && vModel instanceof ReferenceModel)
				return vModel;
			else
				return null;
		}
		else {
			VariableModel<?> newElement = null;

			if(value instanceof IJavaObject) {
				ReferenceModel refElement = new ReferenceModel(jv, isInstance, this);
				Collection<String> tags = ParserManager.getTags(srcFile, jv.getName(), frame.getLineNumber());
				refElement.setTags(tags);
				newElement = refElement;
			}
			else {
				Variable var = getLocalVariable(varName);
				newElement = new ValueModel(jv, isInstance, this, var);
			}

			vars.put(varName, newElement);
			return newElement;
		}
	}

	private void handleArrayIterators() throws DebugException {
		for (Entry<String, VariableModel<?>> e : vars.entrySet()) {
			if(e.getValue() instanceof ReferenceModel) {
				ReferenceModel refModel = (ReferenceModel) e.getValue();
				if(refModel.isPrimitiveArray() && !refModel.isNull()) {
					ArrayPrimitiveModel array = (ArrayPrimitiveModel) refModel.getModelTarget();
					for(String itVar : findArrayIterators(e.getKey())) {
						if(vars.containsKey(itVar))
							array.addVar((ValueModel) vars.get(itVar));
					}
				}
			}
		}
	}


	private Collection<String> findArrayIterators(String pointerVar) throws DebugException {
		List<String> iterators = new ArrayList<>(2);
		for (Variable var : codeAnalysis.variableRoles.values()) {
			if(var.scopeIncludesLine(frame.getLineNumber()) && var instanceof ArrayIterator) {
				ArrayIterator it = (ArrayIterator) var;
				Multimap<Integer, Variable> arrayDimensions = it.getArrayDimensions();
				Collection<Variable> collection = arrayDimensions.get(1);
				for(Variable v : collection)
					if(v.name.equals(pointerVar))
						iterators.add(var.name);
			}
		}
		return iterators;
	}



	public Collection<VariableModel<?>> getInstanceVariables() {
		return vars.values().stream().filter((v) -> !v.isStatic()).collect(Collectors.toList());
	}


	public List<VariableModel<?>> getStaticVariables() {
		return vars.values().stream().filter((v) -> v.isStatic()).collect(Collectors.toList());
	}


	public Collection<ReferenceModel> getReferencesTo(EntityModel<?> object) {
		List<ReferenceModel> refs = new ArrayList<>(3);
		for (ModelElement<?> e : vars.values()) {
			if(e instanceof ReferenceModel && ((ReferenceModel) e).getModelTarget().equals(object))
				refs.add((ReferenceModel) e);
		}
		return refs;
	}


	private String calcString() {
		try {
			IJavaVariable[] localVariables = frame.getLocalVariables();
			int nArgs = frame.getArgumentTypeNames().size();
			List<String> args = new ArrayList<>(localVariables.length);
			for(int i = 0; i < localVariables.length && i < nArgs ; i++) {
				IJavaValue value = (IJavaValue) localVariables[i].getValue();
				args.add(valueToString(value));
			}
			//				args.add(localVariables[i].getValue().getValueString()); // TODO, add special chars '' ", arrays


			if(frame.isStaticInitializer())
				return frame.getDeclaringTypeName() + " (static initializer)";
			else if(frame.isConstructor())
				return "new " + frame.getReferenceType().getName() + "(" + String.join(", ", args) + ")";
			else {
				String ret = frame.getMethodName() + "(" + String.join(", ", args) + ")";
				if(returnValue != null)
					ret += " = " + valueToString(returnValue);
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

			if(value instanceof IJavaObject)
				return runtime.getObject((IJavaObject) value, false, this).toString();

			return value.getValueString();
		}
		catch (DebugException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String valueToString(Value value) {
		if(value instanceof ArrayReference) {
			ArrayReference array = (ArrayReference) value;
			List<Value> values = array.getValues();
			String s = "";
			for(Value v : values) {
				if(!s.isEmpty())
					s += ", ";
				s += valueSpecialChars(v);
			}
			return "{" + s + "}";
		}
		else
			return valueSpecialChars(value);
	}

	private String valueSpecialChars(Value value) {
		if(value instanceof StringReference)
			return "\"" + ((StringReference) value).value() + "\"";
		else if(value instanceof CharValue)
			return "'" + ((CharValue) value).charValue() + "'";
		else
			return value.toString();

	}
	
	public void setReturnValue(Value returnValue) {
		this.returnValue = returnValue;
		obsolete = true;
		setChanged();
		notifyObservers(Collections.emptyList());
	}
	
	public String getInvocationExpression() {
		if(isObsolete() && returnValue != null && !returnValue.toString().equals("(void)"))
			return invExpression + " = " + valueToString(returnValue);
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

		Collection<JavaException> collection = codeAnalysis.lineExceptions.get(line);
		for(JavaException e : collection)
			if(e instanceof ArrayOutOfBounds) {
				ArrayOutOfBounds ae = (ArrayOutOfBounds) e;
				ArrayPrimitiveModel arrayModel = (ArrayPrimitiveModel) ((ReferenceModel) vars.get(ae.arrayName)).getModelTarget();
				arrayModel.setVarError(ae.arrayAccess);
			}

		setChanged();
		notifyObservers(Collections.emptyList());
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

	public RuntimeModel getCallStack() {
		return runtime;
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
			for(VariableModel<?> var : vars.values())
				var.setStep(stepPointer);
		}
		setChanged();
		notifyObservers(Collections.emptyList());
	}

	public int getStepLine() {
		return stepLines.get(stepPointer);
	}

	public RuntimeModel getRuntime() {
		return runtime;
	}





	//	public IValue evalMethod(ObjectModel obj, String expression, boolean addToModel) {
	//		IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();
	//		IWatchExpression newWatchExpression = expressionManager.newWatchExpression(expression);
	//		newWatchExpression.evaluate();
	//
	//		IWatchExpressionDelegate delegate = expressionManager.newWatchExpressionDelegate(frame.getModelIdentifier());
	//		class Wrapper<T> {
	//			T value = null;
	//		};
	//		Wrapper<IValue> res = new Wrapper<>();
	//		Semaphore sem = new Semaphore(0);
	//
	//		IWatchExpressionListener valueListener = result -> {
	//			try {
	//				res.value = result.getValue();
	//			} finally {
	//				sem.release();
	//			}
	//		};
	//		String refName = getReferenceNameTo2(obj);
	//		//		System.out.println(refName);
	//		if(refName == null)
	//			return null;
	//
	//		String exp = refName + "." + expression;
	//		delegate.evaluateExpression(exp, frame, valueListener);
	//		try {
	//			sem.acquire();
	//		} catch (InterruptedException e) {
	//			e.printStackTrace();
	//		}
	//		//		try {
	//		if(res.value != null) {
	//			try {
	//				if(res.value instanceof IJavaPrimitiveValue)
	//					MessageDialog.openInformation(Display.getDefault().getActiveShell(), exp, res.value.getValueString());
	//				else if(res.value instanceof IJavaObject) {
	//					IJavaObject o = (IJavaObject) res.value;
	//					if(!o.isNull() && addToModel) {
	//						getObject(o, addToModel);
	//						setChanged();
	//						notifyObservers();
	//					}
	//				}
	//			}
	//			catch (DebugException e) {
	//				e.printStackTrace();
	//			}
	//		}
	//
	//		return res.value;
	//		//		} catch (DebugException e) {
	//		//			e.printStackTrace();
	//		//		}
	//		//		return null;
	//	}



	//	public void evalMethod2(ObjectModel obj, String expression, IWatchExpressionListener listener) {
	//		IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();
	//
	//		IWatchExpressionDelegate delegate = expressionManager.newWatchExpressionDelegate(frame.getModelIdentifier());
	//
	//		String refName = getReferenceNameTo(obj);
	//		if(refName == null)
	//			return;
	//
	//		delegate.evaluateExpression(refName + "." + expression, frame, listener);
	//	}
	//	
	//	
	//	public String getReferenceNameTo(ModelElement<?> object) {
	//		for(Entry<String, VariableModel<?>> e : vars.entrySet())
	//			if(e.getValue() instanceof ReferenceModel && ((ReferenceModel) e.getValue()).getModelTarget().equals(object))
	//				return e.getKey();
	//
	//		return null;
	//	}


	//	private static class PathVisitor implements SiblingVisitor {
	//	ArrayList<String> path;
	//	boolean found;
	//	EntityModel<?> target;
	//
	//	public PathVisitor(EntityModel<?> target) {
	//		path = new ArrayList<>();
	//		this.target = target;
	//		found = false;
	//	}
	//
	//	public void visit(EntityModel<?> o, ObjectModel parent, int index, int depth, String field) {
	//		if(!found) {
	//			while(path.size() > 0 && path.size() >= depth)
	//				path.remove(path.size()-1);
	//
	//			if(field != null)
	//				path.add(field);
	//
	//			if(o.equals(target))
	//				found = true;
	//		}
	//	}
	//}

	//public String getReferenceNameTo2(EntityModel<?> object) {
	//	for(Entry<String, VariableModel<?>> e : vars.entrySet()) {
	//		if(e.getValue() instanceof ReferenceModel) {
	//			EntityModel<?> el = ((ReferenceModel) e.getValue()).getModelTarget();
	//			if(el instanceof ObjectModel) {
	//				PathVisitor v = new PathVisitor(object);
	//				((ObjectModel) el).traverseSiblings(v, false);
	//				if(v.found)
	//					return  v.path.isEmpty() ? e.getKey() : e.getKey() + "." + String.join(".", v.path);
	//			}
	//		}
	//	}
	//	return null;
	//}


}