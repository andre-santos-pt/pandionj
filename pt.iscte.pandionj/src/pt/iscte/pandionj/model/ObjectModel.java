package pt.iscte.pandionj.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.figures.ObjectFigure;
import pt.iscte.pandionj.figures.StringFigure;

public class ObjectModel extends Observable implements ModelElement {

	private IJavaObject object;
	private StackFrameModel model;
	private Map<String, ValueModel> values;
	private Map<String, ReferenceModel> references;


	private TypeHandler valueHandler = new PrimitiveWrapperHandler();

	public ObjectModel(IJavaObject object, StackFrameModel model) {
		assert object != null;
		this.object = object;
		this.model = model;
		values = new LinkedHashMap<String, ValueModel>();
		references = new LinkedHashMap<String, ReferenceModel>();
		
		try {
		for(IVariable v : object.getVariables()) {
			IJavaVariable var = (IJavaVariable) v;
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
//		references.values().forEach(ref -> ref.update());
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
				.collect(Collectors.toMap(e -> e.getKey(), v -> model.getObject(this, v.getValue().getContent())));
	}



}
