package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.figures.ArrayPrimitiveFigure;

public class ArrayPrimitiveModel extends ArrayModel {

	private Map<String, ValueModel> vars;
	private String varError;

	public ArrayPrimitiveModel(IJavaArray array, StackFrameModel model) {
		super(array, model);
	}

	@Override
	protected void initArray(IJavaArray array) {
		try {
			IJavaType componentType = ((IJavaArrayType) array.getJavaType()).getComponentType();
			assert !(componentType instanceof IJavaReferenceType);
		} catch (DebugException e1) {
			e1.printStackTrace();
		}
		try {
			elements = array.getValues();
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
		vars = new HashMap<>();
	}
	
	
	private Class<?> matchType(IJavaType componentType) {
		try {
			switch(componentType.getName())  {
			case "byte": return Byte.class;
			case "short": return Short.class;
			case "int": return Integer.class;
			case "long": return Long.class;
			case "float": return Float.class;
			case "double": return Double.class;
			case "boolean": return Boolean.class;
			case "char": return Character.class;
			default: throw new AssertionError();
			}
		} catch (DebugException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void update() {
		try {
			IJavaValue[] values = entity.getValues();
			List<Integer> changes = new ArrayList<Integer>();
			for(int i = 0; i < elements.length; i++) {
				boolean equals = values[i].getValueString().equals(elements[i].getValueString());
				if(!equals) {
					elements[i] = values[i];
					changes.add(i);
				}
			}
			if(!changes.isEmpty()) {
				setChanged();
				notifyObservers(changes);
			}
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
	}

	public int getLength() {
		return elements.length;
	}
	
	public String get(int i) {
		return elements[i].toString();
	}

	public Object[] getValues() {
		Object[] array = new Object[elements.length];
		for(int i = 0; i < elements.length; i++)
			array[i] = ((IJavaPrimitiveValue) elements[i]).getIntValue(); // TODO all types
		return array;
	}
	
	
	@Override
	public boolean isPrimitiveType() {
		return true;
	}
	
	public int getInt(int i) {
		if(i < 0 || i >= getLength())
			throw new IndexOutOfBoundsException(Integer.toString(i));

		try {
			return Integer.parseInt(elements[i].getValueString());
		} catch (DebugException e) {
			e.printStackTrace();
			return 0;
		}
		catch (NumberFormatException e) {
			throw new RuntimeException("invalid operation");
		}
	}

	public void addVar(ValueModel v) {
		if(!vars.containsKey(v.getName())) {
			vars.put(v.getName(), v);
			setChanged();
			notifyObservers(v);
		}
	}

	public void setVarError(String var) {
		varError = var;
		setChanged();
		notifyObservers(new RuntimeException(var));
	}

	public Collection<ValueModel> getVars() {
		return Collections.unmodifiableCollection(vars.values());
	}


	@Override
	public String toString() {
		int lim = Math.min(Constants.ARRAY_LENGTH_LIMIT, elements.length);
		String els = "{";
		for(int i = 0; i < lim; i++) {
			if(i != 0)
				els += ", ";
			els += get(i);
		}
		if(lim < elements.length)
			els += ", ...";
		
		els += "}";
		return ArrayPrimitiveModel.class.getSimpleName() + " " + els;
	}
	
	@Override
	protected IFigure createArrayFigure() {
		return new ArrayPrimitiveFigure(this);
	}
}
