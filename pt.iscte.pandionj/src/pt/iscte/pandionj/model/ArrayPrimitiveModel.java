package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.figures.ArrayPrimitiveFigure;

public class ArrayPrimitiveModel extends ArrayModel {

	private List<ValueModel> values;
	
	public ArrayPrimitiveModel(IJavaArray array, StackFrameModel model) {
		super(array, model);
	}

	@Override
	protected void initArray(IJavaArray array) {
		try {
			values = new ArrayList<>(array.getLength());
			for(int i = 0; i < array.getLength(); i++) {
				ValueModel m = new ValueModel((IJavaVariable) array.getVariable(i), false, getStackFrame());
				values.add(m);
			}
		} catch (DebugException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isDecimal() {
		return getComponentType().matches("float|double");
	}
	
	@Override
	protected IFigure createArrayFigure() {
		return new ArrayPrimitiveFigure(this);
	}
	
	boolean updateInternal(int i, int step) {
		return values.get(i).update(step);
	}

	
	public ValueModel getElementModel(int index) {
		return values.get(index);
	}

//	public boolean update(int step) {
//		try {
//			IJavaValue[] values = entity.getValues();
//			List<Integer> changes = new ArrayList<Integer>();
//			for(int i = 0; i < elements.length; i++) {
//				boolean equals = values[i].getValueString().equals(elements[i].getValueString());
//				if(!equals) {
//					elements[i] = values[i];
//					changes.add(i);
//				}
//				this.values.get(i).update(step);
//			}
//			if(!changes.isEmpty()) {
//				setChanged();
//				notifyObservers(changes);
//				return true;
//			}
//		}
//		catch(DebugException e) {
//			e.printStackTrace();
//		}
//		return false;
//	}

	
	
	

//	static Object[] getPrimitiveWrapperValues(IJavaValue[] elements, String type) {
//		Object[] array = new Object[elements.length];
//		switch(type) {
//		case "byte":
//			for(int i = 0; i < elements.length; i++)
//				array[i] = ((IJavaPrimitiveValue) elements[i]).getByteValue();
//			break;
//		
//		case "short":
//			for(int i = 0; i < elements.length; i++)
//				array[i] = ((IJavaPrimitiveValue) elements[i]).getShortValue();
//			break;
//		
//		case "int":
//			for(int i = 0; i < elements.length; i++)
//				array[i] = ((IJavaPrimitiveValue) elements[i]).getIntValue();
//			break;
//		
//		case "long":
//			for(int i = 0; i < elements.length; i++)
//				array[i] = ((IJavaPrimitiveValue) elements[i]).getLongValue();
//			break;
//			
//		case "double":
//			for(int i = 0; i < elements.length; i++)
//				array[i] = ((IJavaPrimitiveValue) elements[i]).getDoubleValue();
//			break;
//		
//		case "float":
//			for(int i = 0; i < elements.length; i++)
//				array[i] = ((IJavaPrimitiveValue) elements[i]).getFloatValue();
//			break;
//		
//		case "char":
//			for(int i = 0; i < elements.length; i++)
//				array[i] = ((IJavaPrimitiveValue) elements[i]).getCharValue();
//			break;
//		
//		case "boolean":
//			for(int i = 0; i < elements.length; i++)
//				array[i] = ((IJavaPrimitiveValue) elements[i]).getBooleanValue();
//			break;	
//			
//		default: throw new AssertionError();
//		}
//		return array;
//	}
	
//	public Object[] getValues() {
//		return getPrimitiveWrapperValues(elements, getComponentType());
//	}


//	public int getInt(int i) {
//		if(i < 0 || i >= getLength())
//			throw new IndexOutOfBoundsException(Integer.toString(i));
//
//		try {
//			return Integer.parseInt(elements[i].getValueString());
//		} catch (DebugException e) {
//			e.printStackTrace();
//			return 0;
//		}
//		catch (NumberFormatException e) {
//			throw new RuntimeException("invalid operation");
//		}
//	}

	
	
//	private Class<?> matchType(IJavaType componentType) {
//		try {
//			switch(componentType.getName())  {
//			case "byte": return Byte.class;
//			case "short": return Short.class;
//			case "int": return Integer.class;
//			case "long": return Long.class;
//			case "float": return Float.class;
//			case "double": return Double.class;
//			case "boolean": return Boolean.class;
//			case "char": return Character.class;
//			default: throw new AssertionError();
//			}
//		} catch (DebugException e) {
//			e.printStackTrace();
//		}
//
//		return null;
//	}
	

}
