package pt.iscte.pandionj.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
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

public class ObjectModel implements ModelElement {

	private IJavaObject object;
	private StackFrameModel model;
	private Map<String, ModelElement> map;
	private Set<String> values;

	private TypeHandler typeHandler = new PrimitiveWrapperHandler();

	public ObjectModel(IJavaObject object, StackFrameModel model) {
		assert object != null;
		this.object = object;
		this.model = model;
		update();
	}

	@Override
	public void update() {
		map = new LinkedHashMap<String, ModelElement>();
		values = new HashSet<>();
		try {
			for(IVariable v : object.getVariables()) {
				IJavaVariable var = (IJavaVariable) v;
				if(!var.isStatic()) {
					String name = var.getName();
					if(!map.containsKey(name) && var.getJavaType() instanceof IJavaReferenceType) {
						map.put(name, model.getObject(this, (IJavaObject) v.getValue()));
						if(typeHandler.qualifies((IJavaValue) v.getValue()))
							values.add(name);
					}
					else {
						map.put(name, new PrimitiveVariableModel(var));
						values.add(name);
					}
				}
			}


		}
		catch(DebugException e) {
			e.printStackTrace();
		}
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
		return Collections.unmodifiableSet(values);
	}

	public String getValue(String field) {
		assert values.contains(field);
		try {
			IJavaValue val = map.get(field).getContent();
			if(typeHandler.qualifies(val))
				return typeHandler.getTextualValue(val);
			else
				return val.getValueString();
		} catch (DebugException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Map<String, ModelElement> getReferences() {
		return map.entrySet().stream().filter(e -> !values.contains(e.getKey())).collect(Collectors.toMap(e -> e.getKey(), v -> v.getValue()));
	}

	

}
