package pt.iscte.pandionj.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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

import pt.iscte.pandionj.figures.ArrayPrimitiveFigure;

public class ArrayPrimitiveModel extends ArrayModel {


	private IJavaArray array;
	private IJavaValue[] elements;

	private Map<String, ValueModel> vars;
	private String varError;

	private Class<?> type;

	public ArrayPrimitiveModel(IJavaArray array) {
		assert array != null;
		try {
			IJavaType componentType = ((IJavaArrayType) array.getJavaType()).getComponentType();
			assert !(componentType instanceof IJavaReferenceType);
			type = matchType(componentType);

		} catch (DebugException e1) {
			e1.printStackTrace();
		}
		try {
			this.array = array;
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
			IJavaValue[] values = array.getValues();
			for(int i = 0; i < elements.length; i++) {
				boolean equals = values[i].equals(elements[i]);
				elements[i] = values[i];
				if(!equals) {
					setChanged();
					notifyObservers(i);
				}
			}
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
	}

	public int getLength() {
		return elements.length;
	}

	@Override
	public int getDimensions() {
		return 1;
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

	@Override
	public IJavaValue getContent() {
		return array;
	}

	public String getComponentType() { // TODO to upper
		IJavaType componentType;
		try {
			componentType = ((IJavaArrayType) array.getJavaType()).getComponentType();
			return componentType.getName();
		} catch (DebugException e) {
			e.printStackTrace();
			return null;
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
	public IFigure createFigure(Graph graph) {
		return new ArrayPrimitiveFigure(this);
	}


//	@Override
//	public String toString() {
//		String els = "{";
//		for(int i = 0; i < elements.length; i++) {
//			if(i != 0)
//				els += ", ";
//			els += get(i);
//		}
//		els += "}";
//		return ArrayPrimitiveModel.class.getSimpleName() + " " + els;
//	}

	@Override
	public void registerObserver(Observer o) {
		addObserver(o);
	}

	
	
}
