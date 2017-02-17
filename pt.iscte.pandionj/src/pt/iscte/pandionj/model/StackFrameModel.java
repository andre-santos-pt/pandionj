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
import java.util.concurrent.Semaphore;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;

import com.google.common.collect.Multimap;

import pt.iscte.pandionj.parser.ParserAPI;
import pt.iscte.pandionj.parser.ParserAPI.ParserResult;
import pt.iscte.pandionj.parser.exception.JavaException;
import pt.iscte.pandionj.parser.exception.JavaException.ArrayOutOfBounds;
import pt.iscte.pandionj.parser.variable.Stepper.ArrayIterator;
import pt.iscte.pandionj.parser.variable.Variable;



public class StackFrameModel extends Observable {
	private IStackFrame stackFrame;
	private Map<String, ModelElement> vars;
	private Map<Long, ModelElement> objects;
	private ParserResult codeAnalysis;

	public StackFrameModel(IStackFrame frame) {
		this.stackFrame = frame;
		vars = new LinkedHashMap<>();
		objects = new HashMap<>();
		IFile srcFile = (IFile) frame.getLaunch().getSourceLocator().getSourceElement(frame);
		codeAnalysis = ParserAPI.parseFile(srcFile.getRawLocation().toString());
	}


	public Variable getLocalVariable(String name) {
		try {
			int line = stackFrame.getLineNumber();
			Collection<Variable> collection = codeAnalysis.variableRoles.get(name);
			for(Variable v : collection) {
				if(v.name.equals(name) && line >= v.scopeLineStart && line <= v.scopeLineEnd) {
					return v;
				}
			}
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void update() {
		handleVariables();
		for(ModelElement e : vars.values())
			e.update();
	}

	private void handleVariables() {
		try {
			Iterator<Entry<String, ModelElement>> iterator = vars.entrySet().iterator();
			while(iterator.hasNext()) {
				String var = iterator.next().getKey();
				boolean contains = false;
				for(IVariable v : stackFrame.getVariables()) {
					if(v.getName().equals(var))
						contains = true;
				}
				if(!contains) {
					iterator.remove();
					setChanged();
					notifyObservers();
				}
			}

			IJavaVariable thisVar = null;
			for(IVariable v : stackFrame.getVariables()) {
				IJavaVariable jv = (IJavaVariable) v;
				String varName = v.getName();

				if(varName.equals("this"))
					thisVar = jv;
				else if(!jv.isSynthetic())
					handleVar(jv);
			}

			if(thisVar != null)
				for (IVariable v : thisVar.getValue().getVariables())
					handleVar((IJavaVariable) v);

			handleArrayIterators();
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
	}



	private void handleArrayIterators() {
		for (Entry<String, ModelElement> e : vars.entrySet()) {
			if(e.getValue().isReference()) {
				ReferenceModel refModel = e.getValue().asReference();
				if(refModel.isArray() && !refModel.isNull()) {
					ArrayModel array = (ArrayModel) refModel.getTarget();
					for(String itVar : findArrayIterators(e.getKey())) {
						if(vars.containsKey(itVar))
							array.addVar(vars.get(itVar).asValue());
					}
				}
			}
		}
	}


	private Collection<String> findArrayIterators(String pointerVar) {
		List<String> iterators = new ArrayList<>(2);
		for (Variable var : codeAnalysis.variableRoles.values()) {
			if(var instanceof ArrayIterator) {
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

	private void handleVar(IJavaVariable jv) throws DebugException {
		String varName = jv.getName();
		IJavaType type = jv.getJavaType();

		if(vars.containsKey(varName)) {
			vars.get(varName).update();
		}
		else {
			ModelElement newElement = type instanceof IJavaReferenceType ? new ReferenceModel(jv, this) : new ValueModel(jv, this);
			vars.put(varName, newElement);

			if(newElement instanceof ReferenceModel)
				((ReferenceModel) newElement).addObserver(new Observer() {

					@Override
					public void update(Observable o, Object arg) {
						setChanged();
						notifyObservers(newElement);
					}
				});

			setChanged();
			notifyObservers(newElement);
		}
	}

	public Collection<ModelElement> getVariables() {
		return Collections.unmodifiableCollection(vars.values());
	}

//	public Collection<ModelElement> getObjects() {
//		return Collections.unmodifiableCollection(objects.values());
//	}

	public String getReferenceNameTo(ModelElement object) {
		for(Entry<String, ModelElement> e : vars.entrySet())
			if(e.getValue() instanceof ReferenceModel && ((ReferenceModel) e.getValue()).getTarget().equals(object))
				return e.getKey();
		
		return null;
	}
	
	public Collection<ReferenceModel> getReferencesTo(ModelElement object) {
		List<ReferenceModel> refs = new ArrayList<>(3);
		for (ModelElement e : vars.values()) {
			if(e instanceof ReferenceModel && ((ReferenceModel) e).getTarget().equals(object))
				refs.add((ReferenceModel) e);
		}
		return refs;
	}
	
	
	public IStackFrame getStackFrame() {
		return stackFrame;
	}


	ModelElement getObject(IJavaObject obj, boolean addToModel) {
		assert !obj.isNull() || !addToModel;
		try {
			ModelElement e = objects.get(obj.getUniqueId());
			if(e == null) {
				if(obj.getJavaType() instanceof IJavaArrayType)
					e = new ArrayModel((IJavaArray) obj);
				else
					e = new ObjectModel(obj, this);

				if(addToModel)
					objects.put(obj.getUniqueId(), e);
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
		Iterator<Entry<Long, ModelElement>> iterator = objects.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<Long, ModelElement> e = iterator.next();
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


	@Override
	public String toString() {
		try {
			String name = stackFrame.getName();
			return (name.equals("<init>") ? "new" : name) + "(...)";
		} catch (DebugException e) {
			e.printStackTrace();
			return super.toString();
		}
	}




	public void processException() {
		try {
			int line = stackFrame.getLineNumber();
			Collection<JavaException> collection = codeAnalysis.lineExceptions.get(line);
			for(JavaException e : collection)
				if(e instanceof ArrayOutOfBounds) {
					ArrayOutOfBounds ae = (ArrayOutOfBounds) e;
					System.out.println(ae.arrayName + " " + ae.arrayAccess);
					ArrayModel arrayModel = (ArrayModel) ((ReferenceModel) vars.get(ae.arrayName)).getTarget();
					arrayModel.setVarError(ae.arrayAccess);
				}
		} catch (DebugException e1) {
			e1.printStackTrace();
		}
	}




	public String evalMethod(ObjectModel obj, String expression) {
		IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();
		IWatchExpression newWatchExpression = expressionManager.newWatchExpression(expression);
		newWatchExpression.evaluate();
		
		IWatchExpressionDelegate delegate = expressionManager.newWatchExpressionDelegate(stackFrame.getModelIdentifier());
		class Wrapper<T> {
			T value = null;
		};
		Wrapper<IValue> res = new Wrapper<>();
		Semaphore sem = new Semaphore(0);

		IWatchExpressionListener valueListener = result -> {
			try {
				res.value = result.getValue();
			} finally {
				sem.release();
			}
		};
		String refName = getReferenceNameTo(obj);
		if(refName == null)
			return null;
		
		delegate.evaluateExpression(refName + "." + expression, stackFrame, valueListener);
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			return res.value == null ? "NULL" : res.value.getValueString();
		} catch (DebugException e) {
			e.printStackTrace();
		}
		return null;
	}


	
	public void evalMethod2(ObjectModel obj, String expression, IWatchExpressionListener listener) {
		IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();
		
		IWatchExpressionDelegate delegate = expressionManager.newWatchExpressionDelegate(stackFrame.getModelIdentifier());

		String refName = getReferenceNameTo(obj);
		if(refName == null)
			return;
		
		delegate.evaluateExpression(refName + "." + expression, stackFrame, listener);
	}



}