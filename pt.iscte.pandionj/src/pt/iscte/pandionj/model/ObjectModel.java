package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;

import pt.iscte.pandionj.ExtensionManager;
import pt.iscte.pandionj.PandionJView;
import pt.iscte.pandionj.ParserManager;
import pt.iscte.pandionj.Utils;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectWidgetExtension;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.extensibility.IVisibleMethod;
import pt.iscte.pandionj.extensibility.PandionJUI;


// TODO debug Exception
public class ObjectModel extends EntityModel<IJavaObject> implements IObjectModel {
	//	private Map<String, ValueModel> values;
	private Map<String, ReferenceModel> references;
	private List<String> refsOfSameType; // TODO from source

	private List<IVariableModel<?>> fields;

	private IType jType;

	private String leftField;
	private String rightField;

	private IObjectWidgetExtension extension;

	public ObjectModel(IJavaObject object, IType type, RuntimeModel runtime) throws DebugException {
		super(object, runtime);
		//		assert type != null : object.toString(); // FIXME java.lang.AssertionError: sun.instrument.InstrumentationImpl (id=23)
		jType = type;
		init(object);
	}

	private void init(IJavaObject object) throws DebugException {
		fields = new ArrayList<IVariableModel<?>>();
		references = new LinkedHashMap<String, ReferenceModel>();
		//		values = new LinkedHashMap<String, ValueModel>();
		refsOfSameType = new ArrayList<>();
		addFields(object);

		if(refsOfSameType.size() == 2) {
			leftField = refsOfSameType.get(0);
			rightField = refsOfSameType.get(1);
		}

		extension = ExtensionManager.getObjectExtension(this);

	}

	private void addFields(IJavaObject object) throws DebugException {

		for(IVariable v : object.getVariables()) {
			IJavaVariable var = (IJavaVariable) v;

			if(!var.isStatic()) {
				String name = var.getName();
				IJavaValue value = (IJavaValue) var.getValue();
				IField f = jType.getField(name);
				boolean visible = isFieldVisible(f);
				VariableModel<?, ?> varModel = null;
				if(value instanceof IJavaObject) {
					ReferenceModel refModel = new ReferenceModel(var, true, visible, null, getRuntimeModel());
					varModel = refModel;

					if(jType != null) {
						IResource resource = jType.getResource();
						if(!jType.isBinary() && resource instanceof IFile) {
							Collection<String> tags = ParserManager.getAttributeTags((IFile) resource, jType.getFullyQualifiedName(), name);
							refModel.setTags(tags);
						}
					}
					references.put(name, refModel);
				}
				else {
					varModel = new ValueModel(var, true, visible, null, getRuntimeModel());
				}

				varModel.registerObserver(new ModelObserver() {
					public void update(Object arg) {
						setChanged();
						notifyObservers(name);
					}
				});
				fields.add(varModel);

				if(!value.isNull() &&  var.getReferenceTypeName().equals(object.getReferenceTypeName()))
					refsOfSameType.add(var.getName());
			}
		}

		fields.sort((a,b) -> {
			if(a.isVisible() && !b.isVisible()) 			return -1;
			else if(b.isVisible() && !a.isVisible()) 	return 1;
			else											return 0;
		});

		int refCount = 0;
		for(IVariableModel<?> var : fields) {
			if(var instanceof IReferenceModel) {
				((IReferenceModel) var).setIndex(refCount++);
			}
		}
	}

	public IType getType() {
		return jType;
	}

	//	public String getTypeName() {
	//		return jType.getFullyQualifiedName();
	//	}

	@Override
	public boolean isNull() {
		return false;
	}

	public boolean includeMethod(IMethod method) {
		return extension.includeMethod(method.getElementName());

	}

	@Override
	public boolean update(int step) {
		for (IVariableModel<?> f : fields) {
			f.update(step);
		}
		//		long valChanges = values.values().stream().filter(val -> val.update(0)).count();
		//		long refChanges = references.values().stream().filter(ref -> ref.update(0)).count();
		//		return (valChanges + refChanges) != 0;
		return true;
	}

	public boolean hasAttributeTags() {
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

	public boolean hasWidgetExtension() {
		return extension != IObjectWidgetExtension.NULL_EXTENSION;
	}

	//	public Set<String> getFieldNames() {
	//		return Collections.unmodifiableSet(values.keySet());
	//	}
	//
	//	public String getValue(String field) {
	//		assert values.containsKey(field);
	//		try {
	//			IJavaValue val = values.get(field).getContent();
	//			return val.getValueString();
	//		} catch (DebugException e) {
	//			e.printStackTrace();
	//		}
	//		return null;
	//	}

	public Map<String, ReferenceModel> getReferences() {
		return Collections.unmodifiableMap(references);
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
			return getContent().getValueString();
		} catch (DebugException e) {
			e.printStackTrace();
			return null;
		}
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


	@Override
	public void setStep(int stepPointer) {
		// TODO stepPointer

	}




	public interface SiblingVisitor {
		void visit(IEntityModel object, ObjectModel parent, int index, int depth, String field);
	}


	public int siblingsDepth() {
		class SiblingVisitorDepth implements SiblingVisitor {
			int max;
			@Override
			public void visit(IEntityModel o, ObjectModel parent, int index, int d, String f) {
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
			public void visit(IEntityModel o, ObjectModel parent, int index, int d, String f) {
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

	private void traverseSiblings(IEntityModel e, ObjectModel parent, int index, Set<IEntityModel> set, SiblingVisitor v, int depth, String field, boolean visitNulls) throws DebugException {
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
					IEntityModel o = refModel.getModelTarget();
					traverseSiblings(o, obj, i++, set, v, depth+1, siblingRef, visitNulls);
				}
			}
		}
	}

	//	private boolean noSiblings() {
	//		for(String siblingRef : refsOfSameType) {
	//			ReferenceModel refModel = references.get(siblingRef);
	//			EntityModel<?> o = refModel.getModelTarget();
	//			if(!(o instanceof NullModel))
	//				return false;
	//		}
	//		return true;
	//	}


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

		IEntityModel leftTarget = obj.references.get(left).getModelTarget();
		IEntityModel rightTarget = obj.references.get(right).getModelTarget();

		return (leftTarget instanceof NullModel || isBinaryTree((ObjectModel) leftTarget, left, right, visited)) &&
				(rightTarget instanceof NullModel || isBinaryTree((ObjectModel) rightTarget, left, right, visited));
	}

	public void infixTraverse(SiblingVisitor visitor, boolean visitNulls) {
		assert isBinaryTree();

		infixTraverse(this, null, null, 0, visitor, visitNulls);
	}



	private void infixTraverse(IEntityModel e, ObjectModel parent, String field, int depth, SiblingVisitor visitor, boolean visitNulls) {
		int index = field == null ? -1 : field.equals(leftField) ? 0 : 1;
		if(e instanceof ObjectModel) {
			ObjectModel obj = (ObjectModel) e;

			IEntityModel leftTarget = obj.references.get(leftField).getModelTarget();
			if(leftTarget instanceof NullModel && visitNulls)
				visitor.visit(leftTarget, obj, index, depth+1, field);
			else
				infixTraverse(leftTarget, obj, leftField, depth+1, visitor, visitNulls);

			visitor.visit(e, parent, index, depth, field);


			IEntityModel rightTarget = obj.references.get(rightField).getModelTarget();
			if(rightTarget instanceof NullModel && visitNulls)
				visitor.visit(rightTarget, obj, index, depth+1, field);
			else
				infixTraverse(rightTarget, obj, rightField, depth+1, visitor, visitNulls);
		}
	}


	public List<IMethod> getInstanceMethods() {
		if(jType == null)
			return Collections.emptyList();

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
			return 	extension.includeMethod(m.getElementName()) && 
					(
							!jType.isMember() && jType.getPackageFragment().isDefaultPackage() && 
							(Flags.isPackageDefault(f) || Flags.isProtected(f) || Flags.isPublic(f))
							||
							Flags.isPublic(f)
							);
		}
		catch (JavaModelException e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean isFieldVisible(IField m) {
		try {
			int f = m.getFlags();
			return 	
					!Flags.isStatic(f) &&
					(
							jType.getPackageFragment().isDefaultPackage() && 
							(Flags.isPackageDefault(f) || Flags.isProtected(f) || Flags.isPublic(f))
							||
							Flags.isPublic(f)
							);
		}
		catch (JavaModelException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public List<IMethod> getVisibleMethods() {
		List<IMethod> list = new ArrayList<>();
		for(IMethod m : getInstanceMethods())
			if(isMethodVisible(m))
				list.add(m);
		return list;
	}

	//	private List<IVariableModel<?>> createVisibleFields() {
	//		List<IVariableModel<?>> vars = new ArrayList<>();
	//		try {
	//			for (IField f : jType.getFields()) {
	//				if(isFieldVisible(f)) {
	//					IJavaFieldVariable field = getContent().getField(f.getElementName(), false);
	//					VariableModel<?, ?> var = null;
	//					if(field.getJavaType() instanceof IJavaReferenceType) {
	//						var = new ReferenceModel(field, true, null, getRuntimeModel());
	//					}
	//					else {
	//						var = new ValueModel(field, true, null, getRuntimeModel());
	//					}
	//					vars.add(var);
	//				}
	//			}
	//		} catch (JavaModelException e) {
	//			e.printStackTrace();
	//		} catch (DebugException e) {
	//			e.printStackTrace();
	//		}
	//		return vars;
	//	}

	public List<IVariableModel<?>> getFields() {
		return Collections.unmodifiableList(fields);
	}



	public void invoke(String methodName, InvocationResult listener, String ... args) {
		IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();
		StackFrameModel stackFrame = getRuntimeModel().getTopFrame();
		IWatchExpressionDelegate delegate = expressionManager.newWatchExpressionDelegate(stackFrame.getStackFrame().getModelIdentifier());

		String exp = methodName + "(" + String.join(", ", args) + ")";

		if(!stackFrame.isInstance()) {
			Collection<IReferenceModel> referencesTo = stackFrame.getReferencesTo(this);
			if(!referencesTo.isEmpty()) {
				exp = referencesTo.iterator().next().getName() + "." + exp;
			}
		}
		IJavaThread thread = (IJavaThread) stackFrame.getStackFrame().getThread();
		delegate.evaluateExpression(exp , stackFrame.getStackFrame(), new ExpressionListener(exp, listener, thread));
	}

	private class ExpressionListener implements IWatchExpressionListener {
		String expression;
		InvocationResult listener;
		IJavaThread thread;
		ExpressionListener(String expression, InvocationResult listener, IJavaThread thread) {
			this.expression = expression;
			this.listener = listener;
			this.thread = thread;
		}

		@Override
		public void watchEvaluationFinished(IWatchExpressionResult result) {
			IJavaValue ret = (IJavaValue) result.getValue();
			DebugException exception = result.getException();
			if(ret != null) {
				if(ret instanceof IJavaObject) {
					IEntityModel object = getRuntimeModel().getObject((IJavaObject) ret, true, null);
					listener.valueReturn(object);
				}
				else
					listener.valueReturn(ret.toString());
				//				processInvocationResult((IJavaValue) result.getValue()); 
			}
			else if(exception != null) {
				String trimExpression = trimExpression(expression);
				String exceptionType = ((InvocationException)exception.getCause()).exception().referenceType().name();
				PandionJUI.executeUpdate(() -> {
					if(exceptionType.equals(IllegalArgumentException.class.getName()))
						MessageDialog.openError(Display.getDefault().getActiveShell(), "Illegal arguments",
								trimExpression + " was invoked with illegal argument values.");
					else if(exceptionType.equals(IllegalStateException.class.getName()))
						MessageDialog.openError(Display.getDefault().getActiveShell(), "Illegal object state",
								"the current state of the object does not allow the operation " + trimExpression);
					else
						MessageDialog.openError(Display.getDefault().getActiveShell(), "Exception occurred", exceptionType + 
							"\nSuggestion: execute " + trimExpression + " through code statements step by step.");
				});
//				System.out.println("EXC: " + exceptionType);
				//				PandionJView.getInstance().handleExceptionBreakpoint(thread, exc);
//				if(exception.getCause() instanceof InvocationException) {
//					InvocationException e = (InvocationException) exception.getCause();
//					ObjectReference exception2 = e.exception();
//					
//				}
			}
		}
		
		String trimExpression(String expression) {
			if(expression.indexOf('.') != -1)
				expression = expression.substring(expression.indexOf('.')+1);
			
			return expression;
		}
	};

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

			IEntityModel t = references.get(fieldName).getModelTarget();
			return t instanceof IArrayModel ? (IArrayModel) t : null;
		} catch (DebugException e) {
			e.printStackTrace();
			return null;
		}
	}
}
