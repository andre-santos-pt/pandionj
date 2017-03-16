package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.Multimap;

import pt.iscte.pandionj.model.ObjectModel.SiblingVisitor;
import pt.iscte.pandionj.parser.ClassInfo;
import pt.iscte.pandionj.parser.JavaSourceParser;
import pt.iscte.pandionj.parser.ParserAPI;
import pt.iscte.pandionj.parser.ParserAPI.ParserResult;
import pt.iscte.pandionj.parser.Visitor;
import pt.iscte.pandionj.parser.exception.JavaException;
import pt.iscte.pandionj.parser.exception.JavaException.ArrayOutOfBounds;
import pt.iscte.pandionj.parser.variable.Stepper.ArrayIterator;
import pt.iscte.pandionj.parser.variable.Variable;



public class StackFrameModel extends Observable {
	private IJavaStackFrame frame;
	private Map<String, ModelElement> vars;
	private Map<Long, ModelElement> objects;
	private ParserResult codeAnalysis;
	private ClassInfo classInfo;

	public StackFrameModel(IJavaStackFrame frame) {
		this.frame = frame;
		vars = new LinkedHashMap<>();
		objects = new HashMap<>();
		IFile srcFile = (IFile) frame.getLaunch().getSourceLocator().getSourceElement(frame);
		try {
			System.out.println("SRC :" + frame.getSourcePath() + " " + frame.isPublic());

			codeAnalysis = ParserAPI.parseFile(srcFile.getRawLocation().toString());
			Visitor visitor = new Visitor();
			JavaSourceParser.createFromFile(srcFile.getRawLocation().toString()).parse(visitor);
			classInfo = visitor.info;
		} 
		catch (DebugException e) {
			e.printStackTrace();
		}
	}


	public Variable getLocalVariable(String name) {
		try {
			int line = frame.getLineNumber();
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

		for(ModelElement o : objects.values().toArray(new ModelElement[objects.size()])) {
			if(o instanceof ArrayModel)
				o.update();
			else if(o instanceof ObjectModel)
				((ObjectModel) o).traverseSiblings(new SiblingVisitor() {

					@Override
					public void accept(ModelElement object, ModelElement parent, int index, int depth, String field) {
						((ObjectModel) object).update();
					}
				});
		}
	}

	private void handleVariables() {
		try {
			Iterator<Entry<String, ModelElement>> iterator = vars.entrySet().iterator();
			while(iterator.hasNext()) {
				String var = iterator.next().getKey();
				boolean contains = false;
				for(IVariable v : frame.getVariables()) {
					String varName = v.getName();
					if(varName.equals(var))
						contains = true;
					else if(varName.equals("this")) {
						for (IVariable iv : v.getValue().getVariables())
							if(iv.getName().equals(var))
								contains = true;
					}
				}
				if(!contains) {
					iterator.remove();
					setChanged();
					notifyObservers();
				}
			}

			for(IVariable v : frame.getVariables()) {
				IJavaVariable jv = (IJavaVariable) v;
				String varName = v.getName();

				if(varName.equals("this")) {
					for (IVariable iv : jv.getValue().getVariables())
						if(!((IJavaVariable) iv).isSynthetic())
							handleVar((IJavaVariable) iv, true);
				}
				else if(!jv.isSynthetic())
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
			vars.get(varName).update();
		}
		else {
			ModelElement newElement = value instanceof IJavaObject ? new ReferenceModel(jv, isInstance, this) : new ValueModel(jv, this);
			vars.put(varName, newElement);
			if(newElement instanceof ReferenceModel)
				((ReferenceModel) newElement).registerObserver(new Observer() {
					public void update(Observable o, Object arg) {
						setChanged();
						notifyObservers(newElement);
					}
				});

			setChanged();
			notifyObservers(newElement);
		}
	}

	private void handleArrayIterators() {
		for (Entry<String, ModelElement> e : vars.entrySet()) {
			if(e.getValue().isReference()) {
				ReferenceModel refModel = e.getValue().asReference();
				if(refModel.isArrayValue() && !refModel.isNull()) {
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



	public Collection<ModelElement> getVariables() {
		HashSet<ModelElement> set = new HashSet<>(vars.values());
		set.addAll(objects.values());
		return set;
		//return Collections.unmodifiableCollection(vars.values());
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

	private static class PathVisitor implements SiblingVisitor {
		ArrayList<String> path;
		boolean found;
		ModelElement target;

		public PathVisitor(ModelElement target) {
			path = new ArrayList<>();
			this.target = target;
			found = false;
		}

		public void accept(ModelElement o, ModelElement parent, int index, int depth, String field) {
			if(!found) {
				while(path.size() > 0 && path.size() >= depth)
					path.remove(path.size()-1);

				if(field != null)
					path.add(field);

				if(o.equals(target))
					found = true;
			}
		}
	}

	public String getReferenceNameTo2(ModelElement object) {
		for(Entry<String, ModelElement> e : vars.entrySet()) {
			if(e.getValue() instanceof ReferenceModel) {
				ModelElement el = ((ReferenceModel) e.getValue()).getTarget();
				if(el instanceof ObjectModel) {
					PathVisitor v = new PathVisitor(object);
					((ObjectModel) el).traverseSiblings(v, false);
					if(v.found)
						return  v.path.isEmpty() ? e.getKey() : e.getKey() + "." + String.join(".", v.path);
				}
			}
		}
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
		return frame;
	}


	public ModelElement getObject(IJavaObject obj, boolean addToModel) {
		assert !obj.isNull() || !addToModel;
		try {
			ModelElement e = objects.get(obj.getUniqueId());
			if(e == null) {
				if(obj.getJavaType() instanceof IJavaArrayType) {
					IJavaType componentType = ((IJavaArrayType) obj.getJavaType()).getComponentType();
					if(componentType instanceof IJavaReferenceType)
						e = new ArrayReferenceModel((IJavaArray) obj, this);

					else
						e = new ArrayModel((IJavaArray) obj);

				}
				else {
					e = new ObjectModel(obj, this, classInfo);
					e.registerObserver(new Observer() {
						public void update(Observable o, Object arg) {
							setChanged();
							notifyObservers();
						}
					});
				}
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
			//			String name = stackFrame.getName();
			return (frame.isConstructor() ? "new " + frame.getReferenceType().getName() : frame.getMethodName())  + "(" + frame.getArgumentTypeNames() + ")";
		} catch (DebugException e) {
			e.printStackTrace();
			return super.toString();
		}
	}

	public void registerObserver(Observer o) {
		addObserver(o);
	}



	public void processException() {
		try {
			int line = frame.getLineNumber();
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




	public IValue evalMethod(ObjectModel obj, String expression, boolean addToModel) {
		IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();
		IWatchExpression newWatchExpression = expressionManager.newWatchExpression(expression);
		newWatchExpression.evaluate();

		IWatchExpressionDelegate delegate = expressionManager.newWatchExpressionDelegate(frame.getModelIdentifier());
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
		String refName = getReferenceNameTo2(obj);
		//		System.out.println(refName);
		if(refName == null)
			return null;

		String exp = refName + "." + expression;
		delegate.evaluateExpression(exp, frame, valueListener);
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//		try {
		if(res.value != null) {
			try {
				if(res.value instanceof IJavaPrimitiveValue)
					MessageDialog.openInformation(Display.getDefault().getActiveShell(), exp, res.value.getValueString());
				else if(res.value instanceof IJavaObject) {
					IJavaObject o = (IJavaObject) res.value;
					if(!o.isNull() && addToModel) {
						getObject(o, addToModel);
						setChanged();
						notifyObservers();
					}
				}
			}
			catch (DebugException e) {
				e.printStackTrace();
			}
		}

		return res.value;
		//		} catch (DebugException e) {
		//			e.printStackTrace();
		//		}
		//		return null;
	}



	public void evalMethod2(ObjectModel obj, String expression, IWatchExpressionListener listener) {
		IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();

		IWatchExpressionDelegate delegate = expressionManager.newWatchExpressionDelegate(frame.getModelIdentifier());

		String refName = getReferenceNameTo(obj);
		if(refName == null)
			return;

		delegate.evaluateExpression(refName + "." + expression, frame, listener);
	}



}