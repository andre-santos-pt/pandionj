package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IEvaluationRunnable;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.zest.core.widgets.Graph;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

import pt.iscte.pandionj.ExtensionManager;
import pt.iscte.pandionj.ParserManager;
import pt.iscte.pandionj.Utils;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectWidgetExtension;
import pt.iscte.pandionj.extensibility.IWidgetExtension;
import pt.iscte.pandionj.figures.ObjectFigure;

public class ObjectModel extends EntityModel<IJavaObject> implements IObjectModel {
	private Map<String, ValueModel> values;
	private Map<String, ReferenceModel> references;
	private List<String> refsOfSameType; // TODO from source

	private IType jType;

	private String leftField;
	private String rightField;

	private IObjectWidgetExtension extension;
	
	public ObjectModel(IJavaObject object, StackFrameModel model) {
		super(object, model);
		assert object != null;
		assert jType != null;
	}

	@Override
	protected void init(IJavaObject object) {
		try {
			jType = getStackFrame().getJavaProject().findType(object.getJavaType().getName());
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		} catch (DebugException e1) {
			e1.printStackTrace();
		}
		values = new LinkedHashMap<String, ValueModel>();
		references = new LinkedHashMap<String, ReferenceModel>();
		refsOfSameType = new ArrayList<>();
		try {
			for(IVariable v : object.getVariables()) {
				IJavaVariable var = (IJavaVariable) v;

				if(!var.isStatic()) {
					String name = var.getName();
					if(var.getValue() instanceof IJavaObject) {
						ReferenceModel refModel = new ReferenceModel(var, true, getStackFrame());
						refModel.registerObserver(new Observer() {
							public void update(Observable o, Object arg) {
								setChanged();
								notifyObservers(name);
							}
						});
// TODO ref change -> new fig
						Collection<String> tags = ParserManager.getAttributeTags(getStackFrame().getSourceFile(), jType.getFullyQualifiedName(), name);
						refModel.setTags(tags);
						references.put(name, refModel);
					}
					else {
						ValueModel val = new ValueModel(var, true, getStackFrame());
						val.registerObserver(new Observer() {
							public void update(Observable o, Object arg) {
								setChanged();
								notifyObservers(name);
							}
						});
						values.put(name, val);
					}
					if(var.getReferenceTypeName().equals(object.getReferenceTypeName()))
						refsOfSameType.add(var.getName());
				}
			}
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
		if(refsOfSameType.size() == 2) {
			leftField = refsOfSameType.get(0);
			rightField = refsOfSameType.get(1);
		}
	}

	public IType getType() {
		return jType;
	}

	public boolean includeMethod(IMethod method) {
		return extension.includeMethod(method.getElementName());

	}

	@Override
	public boolean update(int step) {
		long valChanges = values.values().stream().filter(val -> val.update(0)).count();
		long refChanges = references.values().stream().filter(ref -> ref.update(0)).count();
//		values.values().forEach(val -> val.update(0));
//		references.values().forEach(ref -> ref.update(0));
		return (valChanges + refChanges) != 0;
	}

	@Override
	protected IFigure createInnerFigure(Graph graph) {
		return new ObjectFigure(this, graph, createExtensionFigure(), true);
	}
	
	
	private boolean hasAttributeTags() {
		for(ReferenceModel r : references.values())
			if(r.hasTags())
				return true;
		return false;
	}
	
	
	public Multimap<String, String> getAttributeTags() {
		Multimap<String, String> map = ArrayListMultimap.create();
		
		for(ReferenceModel r : references.values()) {
			if(r.hasTags())
				map.putAll(r.getName(), r.getTags());
		}
		return map;
	}
	
	
	protected IFigure createExtensionFigure() {
		if(hasAttributeTags()) {
			extension = ExtensionManager.createTagExtension(this);
		}
		else if(extension == null) {
			extension = ExtensionManager.getObjectExtension(this);
		}
		
		return extension.createFigure(this);
	}

	public boolean hasWidgetExtension() {
		return extension != IObjectWidgetExtension.NULL_EXTENSION;
	}
	
	public Set<String> getFieldNames() {
		return Collections.unmodifiableSet(values.keySet());
	}

	public String getValue(String field) {
		assert values.containsKey(field);
		try {
			IJavaValue val = values.get(field).getContent();
			return val.getValueString();
		} catch (DebugException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Map<String, ReferenceModel> getReferences() {
		return Collections.unmodifiableMap(references);
	}

	public Collection<ReferenceModel> getReferencePointers() {
		return getStackFrame().getReferencesTo(this);
	}


	public String toStringValue() {
		try {
			return ":" + Utils.toSimpleName(getContent().getReferenceTypeName());
		} catch (DebugException e) {
			e.printStackTrace();
			return "";
		}
		//		MethodInfo m = info.getMethod("toString");
		//		if(m == null)
		//			return "";
		//		
		//		IJavaValue val = invoke(m);
		//		try {
		//			return val == null ? "" : val.getValueString();
		//		} catch (DebugException e) {
		//			e.printStackTrace();
		//			return "";
		//		} 
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
			return invoke4("toString", new IJavaValue[0]).getValueString();
		} catch (DebugException e) {
			e.printStackTrace();
			return super.toString();
		}
		
//		try {
//			return "(" + getContent().getJavaType().getName() + ")";
//			//			String s = toStringValue() + " (" + object.getJavaType().getName() + ")";
//			//			for(Entry<String, ReferenceModel> e : references.entrySet())
//			//				s += "\t" + e.getKey() + " -> " + e.getValue().getContent().toString();
//			//			return s;
//		} catch (DebugException e) {
//			e.printStackTrace();
//			return super.toString();
//		}
	}

	@Override
	public int hashCode() {
		try {
			return (int) getContent().getUniqueId();
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
		void visit(EntityModel<?> object, ObjectModel parent, int index, int depth, String field);
	}


	public int siblingsDepth() {
		class SiblingVisitorDepth implements SiblingVisitor {
			int max;
			@Override
			public void visit(EntityModel<?> o, ObjectModel parent, int index, int d, String f) {
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
			public void visit(EntityModel<?> o, ObjectModel parent, int index, int d, String f) {
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
		return refsOfSameType.size();
	}

	public void traverseSiblings(SiblingVisitor v) {
		traverseSiblings(v, true);
	}

	public void traverseSiblings(SiblingVisitor v, boolean visitNulls) {
		try {
			traverseSiblings(this, null, -1, new HashSet<>(), v, 0, null, visitNulls);
		} catch (DebugException e) {
			e.printStackTrace();
		}
	}

	private void traverseSiblings(EntityModel<?> e, ObjectModel parent, int index, Set<EntityModel<?>> set, SiblingVisitor v, int depth, String field, boolean visitNulls) throws DebugException {
		assert e != null;
		if(!set.contains(e)) {
			set.add(e);
			if(!(e instanceof NullModel) || visitNulls) {
				v.visit(e, parent, index, depth, field);
			}

			if(e instanceof ObjectModel) {
				ObjectModel obj = (ObjectModel) e;
				int i = 0;
				for(String siblingRef : refsOfSameType) {
					ReferenceModel refModel = obj.references.get(siblingRef);
					EntityModel<?> o = refModel.getModelTarget();
					traverseSiblings(o, obj, i++, set, v, depth+1, siblingRef, visitNulls);
				}
			}
		}
	}

	private boolean noSiblings() {
		for(String siblingRef : refsOfSameType) {
			ReferenceModel refModel = references.get(siblingRef);
			EntityModel<?> o = refModel.getModelTarget();
			if(!(o instanceof NullModel))
				return false;
		}
		return true;
	}


	//	public void traverseSiblingsDepth(SiblingVisitor v, boolean visitNulls) {
	//		try {
	//			traverseSiblingsInfix(this, null, -1, new HashSet<>(), v, 0, null, visitNulls);
	//		} catch (DebugException e) {
	//			e.printStackTrace();
	//		}
	//	}
	//
	//	private void traverseSiblingsInfix(ObjectModel obj, ObjectModel parent, int index, Set<ObjectModel> set, SiblingVisitor v, int depth, String field, boolean visitNulls) throws DebugException {
	//		if(set.contains(obj))
	//			return;
	//
	//		if(noSiblings()) {
	//			set.add(obj);
	//			if(visitNulls) {
	//				int i = 0;
	//				for(String siblingRef : refsOfSameType) {
	//					ReferenceModel refModel = obj.references.get(siblingRef);
	//					EntityModel<?>  o = refModel.getModelTarget();
	//					v.visit(null, this, i++, depth+1, siblingRef);
	//				}
	//			}
	//		}
	//		else {
	//			int i = 0;
	//			for(String siblingRef : refsOfSameType) {
	//				ReferenceModel refModel = obj.references.get(siblingRef);
	//				EntityModel<?>  o = refModel.getModelTarget();
	//				if(o instanceof ObjectModel)
	//					traverseSiblings((ObjectModel) o, obj, i++, set, v, depth+1, siblingRef, visitNulls);
	//				else if(o instanceof NullModel && visitNulls)
	//					v.visit(null, this, i++, depth+1, siblingRef);
	//			}
	//			v.visit(obj, parent, index, depth, field);
	//		}
	//	}

	//	private void traverseSiblingsDepth(ObjectModel obj, ObjectModel parent, int index, SiblingVisitor v, int depth, String field, boolean visitNulls) throws DebugException {
	//		Set<ObjectModel> visited = new HashSet<>();
	//		ArrayDeque<ObjectModel> stack = new ArrayDeque<>();
	//		stack.push(obj);
	//
	//		while(!stack.isEmpty()) {
	//			ObjectModel e = stack.pop();
	//			if(!visited.contains(e)) {
	//				visited.add(e);
	//				v.accept(e, parent, index, depth, field);
	//
	//				if(e instanceof ObjectModel)
	//					for(String siblingRef : ((ObjectModel) e).varsOfSameType) {
	//						ReferenceModel refModel = ((ObjectModel) e).references.get(siblingRef);
	//						EntityModel<?> o = refModel.getModelTarget();
	//						//						if(!(o instanceof NullModel && !visitNulls))
	//						stack.push(o);
	//					}
	//
	//			}
	//		}
	//	}


	public boolean isBinaryTree() {
		if(refsOfSameType.size() != 2)
			return false;

		return isBinaryTree(this, refsOfSameType.get(0), refsOfSameType.get(1), new HashSet<>());
	}


	private static boolean isBinaryTree(ObjectModel obj, String left, String right, Set<ObjectModel> visited) {	
		if(visited.contains(obj))
			return false;

		visited.add(obj);

		EntityModel<?> leftTarget = obj.references.get(left).getModelTarget();
		EntityModel<?> rightTarget = obj.references.get(right).getModelTarget();

		return (leftTarget instanceof NullModel || isBinaryTree((ObjectModel) leftTarget, left, right, visited)) &&
				(rightTarget instanceof NullModel || isBinaryTree((ObjectModel) rightTarget, left, right, visited));
	}

	public void infixTraverse(SiblingVisitor visitor, boolean visitNulls) {
		assert isBinaryTree();

		infixTraverse(this, null, null, 0, visitor, visitNulls);
	}

	private void infixTraverse(EntityModel<?> e, ObjectModel parent, String field, int depth, SiblingVisitor visitor, boolean visitNulls) {
		int index = field == null ? -1 : field.equals(leftField) ? 0 : 1;
		if(e instanceof ObjectModel) {
			ObjectModel obj = (ObjectModel) e;

			EntityModel<?> leftTarget = obj.references.get(leftField).getModelTarget();
			if(leftTarget instanceof NullModel && visitNulls)
				visitor.visit(leftTarget, obj, index, depth+1, field);
			else
				infixTraverse(leftTarget, obj, leftField, depth+1, visitor, visitNulls);

			visitor.visit(e, parent, index, depth, field);


			EntityModel<?> rightTarget = obj.references.get(rightField).getModelTarget();
			if(rightTarget instanceof NullModel && visitNulls)
				visitor.visit(rightTarget, obj, index, depth+1, field);
			else
				infixTraverse(rightTarget, obj, rightField, depth+1, visitor, visitNulls);
		}
	}


	public List<IMethod> getInstanceMethods() {
		try {
			List<IMethod> list = new ArrayList<>();
			IMethod[] methods = jType.getMethods();
			for(IMethod m : methods)
			
				if(!m.isConstructor() && !Flags.isStatic(m.getFlags()) && isMethodVisible(m))
					list.add(m);
			return list;
		} catch (JavaModelException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
		//		return info.getMethods(EnumSet.of(VisibilityInfo.PUBLIC));
	}

	private boolean isMethodVisible(IMethod m) {
		try {
			int f = m.getFlags();
			return 	
					!jType.isMember() && jType.getPackageFragment().isDefaultPackage() && 
					(Flags.isPackageDefault(f) || Flags.isProtected(f) || Flags.isPublic(f))
					||
					Flags.isPublic(f);
		}
		catch (JavaModelException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void invoke3(String methodName, IJavaValue[] args, IWatchExpressionListener listener) {
		IMethod method = jType.getMethod(methodName, new String[0]); // TODO
		invoke2(method, args, listener);
	}
	
	public void invoke2(IMethod m, IJavaValue[] args, IWatchExpressionListener listener) {
		IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();
		
		IWatchExpressionDelegate delegate = expressionManager.newWatchExpressionDelegate(getStackFrame().getStackFrame().getModelIdentifier());
		Collection<ReferenceModel> referencesTo = getStackFrame().getReferencesTo(this);
		if(!referencesTo.isEmpty()) {
			String exp = referencesTo.iterator().next().getName() + "." + m.getElementName() + "()";
			delegate.evaluateExpression(exp , getStackFrame().getStackFrame(), listener);
//			IWatchExpression newWatchExpression = expressionManager.newWatchExpression(exp);
//			newWatchExpression.evaluate();
//			IAstEvaluationEngine engine = EvaluationManager.newAstEvaluationEngine(getStackFrame().getJavaProject(), (IJavaDebugTarget) getStackFrame().getStackFrame().getDebugTarget());
//			try {
//				engine.evaluate(exp, getContent(), (IJavaThread) getStackFrame().getStackFrame().getThread(), 
//						new IEvaluationListener() {
//							
//							@Override
//							public void evaluationComplete(IEvaluationResult result) {
//								System.out.println(Arrays.toString(result.getErrorMessages()));
//							}
//						},
//						DebugEvent.EVALUATION, true);
//			} catch (DebugException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}
	
	public IJavaValue invoke4(String methodName, IJavaValue[] args) {
		class Temp {
			IJavaValue val;
		}
		Temp t = new Temp();
		IWatchExpressionListener listener = new IWatchExpressionListener() {
			
			@Override
			public void watchEvaluationFinished(IWatchExpressionResult result) {
				t.val = (IJavaValue) result.getValue();
				synchronized (t) {
					t.notifyAll();					
				}
			}
		};
		
		invoke3(methodName, args, listener);
		
		try {
			synchronized (t) {
				if(t.val == null)
					t.wait();
			}	
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return t.val;
	}


	public IJavaValue invoke(IMethod m, IJavaValue ... args) {
		try {
			IJavaThread t = (IJavaThread) getStackFrame().getStackFrame().getThread();

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
								try {
									ret = getContent().sendMessage(m.getElementName(), m.getSignature(), args, t, false);
								} catch (JavaModelException e) {
									e.printStackTrace();
								}
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

	// TODO: attribute tags
	public Multimap<String, String> getTags() {
		Multimap<String,String> tags = ArrayListMultimap.create();
		
		for (Entry<String, ReferenceModel> e : references.entrySet()) {
			for(String t : e.getValue().getTags())
				tags.put(e.getKey(), t);
		}
		return tags;
	}
	

	@Override
	public String getStringValue() {
		try {
			return getContent().getValueString();
		} catch (DebugException e) {
			e.printStackTrace();
			return "";
		}
	}

	public int getInt(String fieldName) {
		try {
			IJavaFieldVariable field = getContent().getField(fieldName, false);
			if(field == null)
				throw new IllegalArgumentException(fieldName + " is not a field");

			if(!field.getJavaType().getName().equals("int"))
				throw new IllegalAccessError(fieldName + " is not of type int");

			IJavaPrimitiveValue value = (IJavaPrimitiveValue) field.getValue();
			return value.getIntValue();

		} catch (DebugException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public IArrayModel getArray(String fieldName) {
		try {
			IJavaFieldVariable field = getContent().getField(fieldName, false);
			if(field == null)
				throw new IllegalArgumentException(fieldName + " is not a field");

			//			if(!(field.getJavaType() instanceof IJavaArrayType))
			//				throw new IllegalArgumentException(fieldName + " is not an array field");

			EntityModel<?> t = references.get(fieldName).getModelTarget();
			return t instanceof IArrayModel ? (IArrayModel) t : null;
		} catch (DebugException e) {
			e.printStackTrace();
			return null;
		}
	}

	

}
