package pt.iscte.pandionj.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import pt.iscte.pandionj.figures.ObjectFigure;
import pt.iscte.pandionj.figures.StringFigure;

public class ObjectModel extends Observable implements ModelElement {

	private IJavaObject object;
	private StackFrameModel model;
	private Map<String, ValueModel> values;
	private Map<String, ReferenceModel> references;
	private List<String> varsOfSameType;

	private TypeHandler valueHandler = new PrimitiveWrapperHandler();

	public ObjectModel(IJavaObject object, StackFrameModel model) {
		assert object != null;

		this.object = object;
		this.model = model;
		values = new LinkedHashMap<String, ValueModel>();
		references = new LinkedHashMap<String, ReferenceModel>();
		varsOfSameType = new ArrayList<>();
		try {
			for(IVariable v : object.getVariables()) {
				IJavaVariable var = (IJavaVariable) v;
				if(var.getJavaType().equals(object.getJavaType()))
					varsOfSameType.add(var.getName());

				if(!var.isStatic()) {
					String name = var.getName();
					if(var.getJavaType() instanceof IJavaReferenceType && !valueHandler.qualifies((IJavaValue) v.getValue())) {
						references.put(name, new ReferenceModel(var, model));
					}
					else {
						ValueModel val = new ValueModel(var, model);
						val.addObserver(new Observer() {

							@Override
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
	}

	@Override
	public IJavaValue getContent() {
		return object;
	}

	@Override
	public IFigure createFigure() {
		try {
			if(object.getJavaType().getName().equals(String.class.getName()))
				return new StringFigure(object.getValueString());
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
		return new ObjectFigure(this);
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
		return model.getReferencesTo(this);
	}

	//	public String eval(String expression) {
	//		return model.eval(thisexpression);
	//	}

	public String toStringValue() {
		return model.evalMethod(this, "toString()");
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
		return 
				obj instanceof ObjectModel &&
				((ObjectModel) obj).hashCode() == hashCode();
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

}
