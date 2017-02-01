package pt.iscte.pandionj.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Semaphore;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;



public class StackFrameModel extends Observable {
	private IStackFrame stackFrame;
	private Map<String, ModelElement> vars;
	private Map<Long, ModelElement> objects;

	public StackFrameModel(IStackFrame stackFrame) {
		this.stackFrame = stackFrame;
		vars = new LinkedHashMap<>();
		objects = new HashMap<>();
		handleVariables();
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
				else
					handleVar(jv);
			}
			
			if(thisVar != null)
				for (IVariable v : thisVar.getValue().getVariables())
					handleVar((IJavaVariable) v);

		}
		catch(DebugException e) {
			e.printStackTrace();
		}
	}

	private void handleVar(IJavaVariable jv) throws DebugException {
		String varName = jv.getName();
		IJavaType type = jv.getJavaType();

		if(vars.containsKey(varName)) {
			vars.get(varName).update();
		}
		else {
			ModelElement newElement = type instanceof IJavaReferenceType ? new ReferenceModel(jv, this) : new ValueModel(jv);
			vars.put(varName, newElement);

			if(newElement instanceof ReferenceModel)
				((ReferenceModel) newElement).addObserver(new Observer() {

					@Override
					public void update(Observable o, Object arg) {
						setChanged();
						notifyObservers(newElement);
					}
				});

			// TODO TEST
//			if(varName.equals("next"))
//				((ArrayModel) ((ReferenceModel) vars.get("elements")).getTarget()).addVar((PrimitiveVariableModel) newElement);

			setChanged();
			notifyObservers(newElement);
		}
	}

	public Collection<ModelElement> getVariables() {
		return Collections.unmodifiableCollection(vars.values());
	}

	public Collection<ModelElement> getObjects() {
		return Collections.unmodifiableCollection(objects.values());
//		List<ModelElement> objs = new ArrayList<>();
//		for(ModelElement e : vars.values())
//			if(e instanceof ReferenceModel) {
//				ModelElement t = ((ReferenceModel) e).getTarget();
//				objs.add(t);
//			}
//
//
//		return objs;
	}

	public IStackFrame getStackFrame() {
		return stackFrame;
	}


	 ModelElement getObject(ModelElement pointer, IJavaObject obj) {
		try {
			if(obj.isNull())
				return NullModel.getInstance(pointer, obj);

			ModelElement e = objects.get(obj.getUniqueId());
			if(e == null) {
				if(obj.getJavaType() instanceof IJavaArrayType)
					e = new ArrayModel((IJavaArray) obj);
				else
					e = new ObjectModel(obj, this);

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
			return stackFrame.getName();
		} catch (DebugException e) {
			e.printStackTrace();
			return super.toString();
		}
	}









	public String eval(String expression) {
		IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();
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
		delegate.evaluateExpression(expression, stackFrame, valueListener);
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res.value == null ? "NULL" : res.value.toString();
	}

	
}