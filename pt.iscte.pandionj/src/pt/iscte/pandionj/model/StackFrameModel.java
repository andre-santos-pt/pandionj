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
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.Multimap;
import com.sun.jdi.Value;

import pt.iscte.pandionj.ParserManager;
import pt.iscte.pandionj.model.ObjectModel.SiblingVisitor;
import pt.iscte.pandionj.parser.ParserAPI.ParserResult;
import pt.iscte.pandionj.parser.exception.JavaException;
import pt.iscte.pandionj.parser.exception.JavaException.ArrayOutOfBounds;
import pt.iscte.pandionj.parser.variable.Stepper.ArrayIterator;
import pt.iscte.pandionj.parser.variable.Variable;



@SuppressWarnings("restriction")
public class StackFrameModel extends Observable {
	private IJavaStackFrame frame;
	private Map<String, VariableModel<?>> vars;
	private Map<Long, EntityModel<?>> objects;
	private Map<Long, EntityModel<?>> looseObjects;

	private ParserResult codeAnalysis;
	private IFile srcFile;
	private IJavaProject javaProject;

	private String invExpression;
	private Value returnValue;

	private boolean obsolete;
	private String exceptionType;

	private CallStackModel parent;

	private int instant; // TODO

//	private StackFrameImpl underlyingFrame;

	public StackFrameModel(CallStackModel parent, IJavaStackFrame frame) {
//		JDIStackFrame jdif = (JDIStackFrame) frame;
//		try {
//			Field field = JDIStackFrame.class.getDeclaredField("fStackFrame");
//			field.setAccessible(true);
//			underlyingFrame = (StackFrameImpl) field.get(jdif);
//		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
//			e.printStackTrace();
//		} 
		
		this.parent = parent;
		this.frame = frame;
		vars = new LinkedHashMap<>();
		objects = new HashMap<>();
		looseObjects = new HashMap<>();
		srcFile = (IFile) frame.getLaunch().getSourceLocator().getSourceElement(frame);
		codeAnalysis = ParserManager.getParserResult(srcFile);
		javaProject = JavaCore.create(srcFile.getProject());

		obsolete = false;
		exceptionType = null;
	}

	public boolean isExecutionFrame() {
		return !parent.isEmpty() && parent.getTopFrame() == this;
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
		handleVariables();

		for(VariableModel<?> e : vars.values())
			e.update(0);

		for(EntityModel<?> o : objects.values().toArray(new EntityModel[objects.size()])) {
			if(o instanceof ArrayModel)
				o.update(0);
			else if(o instanceof ObjectModel)
				((ObjectModel) o).traverseSiblings(new SiblingVisitor() {
					public void visit(EntityModel<?> object, ObjectModel parent, int index, int depth, String field) {
						if(object != null)
							object.update(0);
					}
				});
		}
		setChanged();
		notifyObservers();
	}

	public void setReturnValue(Value returnValue) {
		this.returnValue = returnValue;
		obsolete = true;
		setChanged();
		notifyObservers();
	}

	public void setObsolete() {
		obsolete = true;
		setChanged();
		notifyObservers();
	}

	public Object getReturnValue() {
		assert returnValue != null;
		return returnValue.toString();
	}

	private void handleVariables() {
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
			notifyObservers();

			for(IVariable v : frame.getVariables()) {
				IJavaVariable jv = (IJavaVariable) v;
				String varName = v.getName();

				if(varName.equals("this")) {
					for (IVariable iv : jv.getValue().getVariables())
						if(!((IJavaVariable) iv).isSynthetic() && !((IJavaVariable) iv).isStatic())
							handleVar((IJavaVariable) iv, true);
				}
				else if(!jv.isSynthetic()) //&& !jv.isStatic())
					handleVar(jv, false);
			}

			handleArrayIterators();
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
	}


	private void handleVar(IJavaVariable jv, boolean isInstance) throws DebugException {
		String varName = jv.getName();

		IJavaValue value = (IJavaValue) jv.getValue();
		//		IJavaType type = jv.getJavaType();

		if(vars.containsKey(varName)) {
			vars.get(varName).update(0);
		}
		else {
			VariableModel<?> newElement = null;
			
			if(value instanceof IJavaObject) {
				ReferenceModel refElement = new ReferenceModel(jv, isInstance, this);
				refElement.registerObserver(new Observer() {
					public void update(Observable o, Object arg) {
						setChanged();
						notifyObservers(refElement);
					}
				});
				
				Collection<String> tags = ParserManager.getTags(srcFile, jv.getName(), frame.getLineNumber());
				refElement.setTags(tags);
				newElement = refElement;
			}
			else {
				newElement = new ValueModel(jv, isInstance, this);
			}

			vars.put(varName, newElement);				
			setChanged();
			notifyObservers(newElement);
		}
	}

	private void handleArrayIterators() throws DebugException {
		for (Entry<String, VariableModel<?>> e : vars.entrySet()) {
			if(e.getValue() instanceof ReferenceModel) {
				ReferenceModel refModel = (ReferenceModel) e.getValue();
				if(refModel.isArrayValue() && !refModel.isNull()) {
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



	public Collection<VariableModel<?>> getVariables() {
		return Collections.unmodifiableCollection(vars.values());
	}




	public Collection<ReferenceModel> getReferencesTo(ModelElement<?> object) {
		List<ReferenceModel> refs = new ArrayList<>(3);
		for (ModelElement<?> e : vars.values()) {
			if(e instanceof ReferenceModel && ((ReferenceModel) e).getModelTarget().equals(object))
				refs.add((ReferenceModel) e);
		}
		return refs;
	}



	public EntityModel<? extends IJavaObject> getObject(IJavaObject obj, boolean loose) {
		assert !obj.isNull();
		try {
			EntityModel<? extends IJavaObject> e = objects.get(obj.getUniqueId());
			if(e == null) {
				if(obj.getJavaType() instanceof IJavaArrayType) {
					IJavaType componentType = ((IJavaArrayType) obj.getJavaType()).getComponentType();
					if(componentType instanceof IJavaReferenceType)
						e = new ArrayReferenceModel((IJavaArray) obj, this);
					else
						e = new ArrayPrimitiveModel((IJavaArray) obj, this);
				}
				else {
					e = new ObjectModel(obj, this);
				}

				if(loose) {
					looseObjects.put(obj.getUniqueId(), e);
				}
				else {
					objects.put(obj.getUniqueId(), e);
				}
				setChanged();
				notifyObservers(e);
			}
			return e;
		}
		catch(DebugException e) {
			e.printStackTrace();
			return null;
		}
	}




	public void simulateGC() {
		boolean removals = false;
		Iterator<Entry<Long, EntityModel<?>>> iterator = objects.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<Long, EntityModel<?>> e = iterator.next();
			if(!vars.containsValue(e.getValue())) {
				iterator.remove();
				removals = true;
			}
		}
		if(removals) {
			setChanged();
			notifyObservers();
		}

	}



	private String calcString() {
		try {
			IJavaVariable[] localVariables = frame.getLocalVariables();
			int nArgs = frame.getArgumentTypeNames().size();
			List<String> args = new ArrayList<>(localVariables.length);
			for(int i = 0; i < localVariables.length && i < nArgs ; i++)
				args.add(localVariables[i].getValue().getValueString()); // TODO, add special chars '' ", arrays


			if(frame.isStaticInitializer())
				return frame.getDeclaringTypeName();
			else if(frame.isConstructor())
				return "new " + frame.getReferenceType().getName();
			else {
				String ret = frame.getMethodName() + "(" + String.join(", ", args) + ")";
				if(returnValue != null)
					ret += " = " + returnValue.toString();
				return ret;
			}
		} catch (DebugException e) {
			e.printStackTrace();
			return super.toString();
		}
	}


	public String getInvocationExpression() {
		if(isObsolete() && returnValue != null && !returnValue.toString().equals("(void)"))
			return invExpression + " = " + returnValue.toString();
		else if(invExpression == null && !isObsolete()) {
			invExpression = calcString();
			return invExpression;
		}
		else if(invExpression != null)
			return invExpression;
		else
			return toString();
	}



	public void registerObserver(Observer o) {
		addObserver(o);
	}

	public void registerDisplayObserver(Observer obs, Control control) {
		addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				Display.getDefault().asyncExec(() -> {
					obs.update(o, arg);
				});
			}
		});

		control.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				deleteObserver(obs);
			}
		});
	}


	public void processException(String exceptionType, int line) {
		//		if(exception.equals(ArrayIndexOutOfBoundsException.class.getName()))
		this.exceptionType = exceptionType;

		Collection<JavaException> collection = codeAnalysis.lineExceptions.get(line);
		for(JavaException e : collection)
			if(e instanceof ArrayOutOfBounds) {
				ArrayOutOfBounds ae = (ArrayOutOfBounds) e;
				ArrayPrimitiveModel arrayModel = (ArrayPrimitiveModel) ((ReferenceModel) vars.get(ae.arrayName)).getModelTarget();
				arrayModel.setVarError(ae.arrayAccess);
			}

		setChanged();
		notifyObservers();
	}



	public void objectReferenceChanged() {
		setChanged();
		notifyObservers();
	}

	public IJavaProject getJavaProject() {
		return javaProject;
	}

	public Collection<EntityModel<?>> getLooseObjects() {
		return Collections.unmodifiableCollection(looseObjects.values());
		//		Collection<EntityModel<?>> referencedObjects = getReferencedObjects();
		//		List<EntityModel<?>> list = new ArrayList<>();
		//		
		//		for (EntityModel<?> m : objects.values())
		//			if(!referencedObjects.contains(m))
		//				list.add(m);
		//		return list;
	}

	private Collection<EntityModel<?>> getReferencedObjects() {
		List<EntityModel<?>> list = new ArrayList<>();
		for (VariableModel<?> var : vars.values()) {
			if(var instanceof ReferenceModel)
				list.add(((ReferenceModel) var).getModelTarget());
		}
		return list;
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

	public CallStackModel getCallStack() {
		return parent;
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