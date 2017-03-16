package pt.iscte.pandionj.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Renderer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IEvaluationRunnable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.core.widgets.Graph;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;

import pt.iscte.pandionj.figures.ObjectFigure;
import pt.iscte.pandionj.figures.StringFigure;
import pt.iscte.pandionj.parser.ClassInfo;
import pt.iscte.pandionj.parser.MethodInfo;
import pt.iscte.pandionj.parser.VisibilityInfo;

public class ObjectModel extends Observable  implements ModelElement {

	private IJavaObject object;
	private StackFrameModel frame;
	private Map<String, ValueModel> values;
	private Map<String, ReferenceModel> references;
	private List<String> varsOfSameType;

	private TypeHandler valueHandler = new PrimitiveWrapperHandler();

	private ClassInfo info;

	public ObjectModel(IJavaObject object, StackFrameModel model, ClassInfo info) {
		assert object != null;
		this.object = object;
		this.frame = model;
		this.info = info;

		values = new LinkedHashMap<String, ValueModel>();
		references = new LinkedHashMap<String, ReferenceModel>();
		varsOfSameType = new ArrayList<>();
		try {
			for(IVariable v : object.getVariables()) {
				IJavaVariable var = (IJavaVariable) v;

				//				if(!value.isNull() && var.getJavaType().equals(object.getJavaType()))
				//					varsOfSameType.add(var.getName());
				if(!var.isStatic() && var.getReferenceTypeName().equals(object.getReferenceTypeName()))
					varsOfSameType.add(var.getName());

				if(!var.isStatic()) {
					String name = var.getName();
					if(var.getValue() instanceof IJavaObject && !valueHandler.qualifies((IJavaValue) v.getValue())) {
						ReferenceModel refModel = new ReferenceModel(var, true, model);
						refModel.registerObserver(new Observer() {
							public void update(Observable o, Object arg) {
								setChanged();
								notifyObservers(name);
							}
						});
						references.put(name, refModel);
					}
					else {
						ValueModel val = new ValueModel(var, model);
						val.registerObserver(new Observer() {
							public void update(Observable o, Object arg) {
								setChanged();
								notifyObservers(name);
							}
						});
						values.put(name, val);
					}
				}
			}
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update() {
		values.values().forEach(val -> val.update());
		references.values().forEach(ref -> ref.update());
	}

	@Override
	public IJavaValue getContent() {
		return object;
	}

	@Override
	public IFigure createFigure(Graph graph) {
		try {
			if(object.getJavaType().getName().equals(String.class.getName()))
				return new StringFigure(object.getValueString());
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
		return new ObjectFigure(this, graph);
	}

	public Set<String> getFields() {
		return Collections.unmodifiableSet(values.keySet());
	}

	public String getValue(String field) {
		assert values.containsKey(field);
		try {
			IJavaValue val = values.get(field).getContent();
			if(valueHandler.qualifies(val))
				return valueHandler.getTextualValue(val);
			else
				return val.getValueString();
		} catch (DebugException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Map<String, ModelElement> getReferences() {
		return references.entrySet().stream()
				.filter(e -> !values.containsKey(e.getKey()))
				.collect(Collectors.toMap(e -> e.getKey(), v -> v.getValue().getTarget()));

	}

	public Collection<ReferenceModel> getReferencePointers() {
		return frame.getReferencesTo(this);
	}

	//	public String eval(String expression) {
	//		return model.eval(thisexpression);
	//	}

	public String toStringValue() {
		MethodInfo m = info.getMethod("toString");
		if(m == null)
			return "";
		
		IJavaValue val = invoke(m);
		try {
			return val == null ? "" : val.getValueString();
		} catch (DebugException e) {
			e.printStackTrace();
			return "";
		} 
		//		try {
		//			IValue val = model.evalMethod(this, "toString()", false);
		//			return val != null ? val.getValueString() : "NULL";
		//		} catch (DebugException e) {
		//			e.printStackTrace();
		//			return "NULL";
		//		}
	}

	@Override
	public String toString() {
		try {
			return "(" + object.getJavaType().getName() + ")";
			//			String s = toStringValue() + " (" + object.getJavaType().getName() + ")";
			//			for(Entry<String, ReferenceModel> e : references.entrySet())
			//				s += "\t" + e.getKey() + " -> " + e.getValue().getContent().toString();
			//			return s;
		} catch (DebugException e) {
			e.printStackTrace();
			return super.toString();
		}
	}

	@Override
	public int hashCode() {
		try {
			return (int) object.getUniqueId();
		} catch (DebugException e) {
			e.printStackTrace();
			return super.hashCode();
		}
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ObjectModel && ((ObjectModel) obj).hashCode() == hashCode();
	}


	public interface SiblingVisitor {
		void accept(ModelElement object, ModelElement parent, int index, int depth, String field);
	}


	public int siblingsDepth() {
		class SiblingVisitorDepth implements SiblingVisitor {
			int max;
			@Override
			public void accept(ModelElement o, ModelElement parent, int index, int d, String f) {
				max = Math.max(max, d);
			}
		};
		SiblingVisitorDepth v = new SiblingVisitorDepth();
		traverseSiblings(v);
		return v.max;
	}

	public int siblingsBreath() {
		class SiblingVisitorBreath implements SiblingVisitor {
			Multiset<Integer> count = HashMultiset.create();
			public void accept(ModelElement o, ModelElement parent, int index, int d, String f) {
				count.add(d);
			}
			public int max() {
				Optional<Integer> opt = count.elementSet().stream().map((i) -> count.count(i)).max((a,b) -> Integer.compare(a, b));
				return opt.isPresent() ? opt.get() : 1;
			}
		};
		SiblingVisitorBreath v = new SiblingVisitorBreath();
		traverseSiblings(v);
		return v.max();
	}

	public int numberOfSiblings() {
		return varsOfSameType.size();
	}

	public void traverseSiblings(SiblingVisitor v) {
		traverseSiblings(v, false);
	}

	public void traverseSiblings(SiblingVisitor v, boolean visitNulls) {
		try {
			traverseSiblings(this, null, -1, new HashSet<>(), v, 0, null, visitNulls);
		} catch (DebugException e) {
			e.printStackTrace();
		}
	}

	private void traverseSiblings(ObjectModel obj, ObjectModel parent, int index, Set<ObjectModel> set, SiblingVisitor v, int depth, String field, boolean visitNulls) throws DebugException {
		if(!set.contains(obj)) {
			set.add(obj);
			v.accept(obj, parent, index, depth, field);
			int i = 0;
			for(String siblingRef : varsOfSameType) {
				ReferenceModel refModel = obj.references.get(siblingRef);
				ModelElement o = refModel.getTarget();
				if(o instanceof ObjectModel)
					traverseSiblings((ObjectModel) o, obj, i++, set, v, depth+1, siblingRef, visitNulls);
				else if(o instanceof NullModel && visitNulls)
					v.accept(o, parent, i++, depth+1, siblingRef);
			}
		}
	}

	private boolean noSiblings() {
		for(String siblingRef : varsOfSameType) {
			ReferenceModel refModel = references.get(siblingRef);
			ModelElement o = refModel.getTarget();
			if(!(o instanceof NullModel))
				return false;
		}
		return true;
	}


	public void traverseSiblingsDepth(SiblingVisitor v, boolean visitNulls) {
		try {
			traverseSiblingsInfix(this, null, -1, new HashSet<>(), v, 0, null, visitNulls);
		} catch (DebugException e) {
			e.printStackTrace();
		}
	}

	private void traverseSiblingsInfix(ObjectModel obj, ObjectModel parent, int index, Set<ObjectModel> set, SiblingVisitor v, int depth, String field, boolean visitNulls) throws DebugException {
		if(set.contains(obj))
			return;

		if(noSiblings()) {
			set.add(obj);
			if(visitNulls) {
				int i = 0;
				for(String siblingRef : varsOfSameType) {
					ReferenceModel refModel = obj.references.get(siblingRef);
					ModelElement o = refModel.getTarget();
					v.accept(o, this, i++, depth+1, siblingRef);
				}
			}
		}
		else {
			int i = 0;
			for(String siblingRef : varsOfSameType) {
				ReferenceModel refModel = obj.references.get(siblingRef);
				ModelElement o = refModel.getTarget();
				if(o instanceof ObjectModel)
					traverseSiblings((ObjectModel) o, obj, i++, set, v, depth+1, siblingRef, visitNulls);
				else if(o instanceof NullModel && visitNulls)
					v.accept(o, this, i++, depth+1, siblingRef);
			}
			v.accept(obj, parent, index, depth, field);
		}
	}

	private void traverseSiblingsDepth(ObjectModel obj, ObjectModel parent, int index, SiblingVisitor v, int depth, String field, boolean visitNulls) throws DebugException {
		Set<ModelElement> visited = new HashSet<>();
		ArrayDeque<ModelElement> stack = new ArrayDeque<>();
		stack.push(obj);

		while(!stack.isEmpty()) {
			ModelElement e = stack.pop();
			if(!visited.contains(e)) {
				visited.add(e);
				v.accept(e, parent, index, depth, field);

				if(e instanceof ObjectModel)
					for(String siblingRef : ((ObjectModel) e).varsOfSameType) {
						ReferenceModel refModel = ((ObjectModel) e).references.get(siblingRef);
						ModelElement o = refModel.getTarget();
						//						if(!(o instanceof NullModel && !visitNulls))
						stack.push(o);
					}

			}
		}
	}


	public boolean isBinaryTree() {
		if(varsOfSameType.size() != 2)
			return false;

		return isBinaryTree(this, varsOfSameType.get(0), varsOfSameType.get(1), new HashSet<>());
	}


	private static boolean isBinaryTree(ObjectModel obj, String left, String right, Set<ObjectModel> visited) {	
		if(visited.contains(obj))
			return false;
		else
			visited.add(obj);

		ModelElement leftTarget = obj.references.get(left).getTarget();
		ModelElement rightTarget = obj.references.get(right).getTarget();

		return (leftTarget instanceof NullModel || isBinaryTree((ObjectModel) leftTarget, left, right, visited)) &&
				(rightTarget instanceof NullModel || isBinaryTree((ObjectModel) rightTarget, left, right, visited));
	}

	public void infixTraverse(SiblingVisitor v) {
		assert isBinaryTree();

		infixTraverse(this, varsOfSameType.get(0), varsOfSameType.get(1), 0,  v);
	}

	private static void infixTraverse(ObjectModel obj, String left, String right, int depth, SiblingVisitor v) {
		ModelElement leftTarget = obj.references.get(left).getTarget();
		if(leftTarget instanceof ObjectModel)
			infixTraverse((ObjectModel) leftTarget, left, right, depth+1, v);
		else
			v.accept(leftTarget, null, -1, depth+1, null);

		v.accept(obj, null, -1, depth, null);

		ModelElement rightTarget = obj.references.get(right).getTarget();
		if(rightTarget instanceof ObjectModel)
			infixTraverse((ObjectModel) rightTarget, left, right, depth+1, v);
		else
			v.accept(rightTarget, null, -1, depth+1, null);
	}

	@Override
	public void registerObserver(Observer o) {
		addObserver(o);
	}

	public StackFrameModel getStackFrame() {
		return frame;
	}

	public Iterator<MethodInfo> getMethods() {
		return info.getMethods(EnumSet.of(VisibilityInfo.PUBLIC));
	}

	public IJavaValue invoke(MethodInfo m, IJavaValue ... args) {
		System.out.println("inv");
		try {
			IJavaThread t = (IJavaThread) frame.getStackFrame().getThread();
			
			class MethodCall implements IEvaluationRunnable, ITerminate {
				IJavaValue ret;
				
				Thread job;
				
				@Override
				public boolean canTerminate() {
					return true;
				}

				@Override
				public boolean isTerminated() {
					return !job.isAlive();
				}

				@Override
				public void terminate() throws DebugException {
					job.stop();
				}

				@Override
				public void run(IJavaThread thread, IProgressMonitor monitor) throws DebugException {
					job = new Thread() {
						public void run() {
							try {
								ret = object.sendMessage(m.getName(), m.getJNISignature(), args, t, false);
							} catch (DebugException e) {
								e.printStackTrace();
							}
						};
					};
					job.start();
					try {
						job.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			}
		
			MethodCall call = new MethodCall();
			t.runEvaluation(call, null, DebugEvent.EVALUATION, true);
			
			return call.ret;
		} catch (DebugException e) {
//			ObjectReference exception = ((InvocationException) e.getCause()).exception();
//			((InvocationException) e.getCause()).printStackTrace();
//			System.out.println(exception);
			return null;
		}
	}

//	public IJavaValue invoke(String methodName, String returnType, IJavaValue ... args) {
//		String signature = JNIUtil.genJNISignature(returnType, args);
//
//		try {
//			return object.sendMessage(methodName, signature, args, (IJavaThread) model.getStackFrame().getThread(), null);
//		} catch (DebugException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
}
