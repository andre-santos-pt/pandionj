package pt.iscte.pandionj.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.PandionJView;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IVariableModel;

public abstract class ArrayModel<T extends IVariableModel<?>> 
extends EntityModel<IJavaArray> implements IArrayModel<T> {

	private IJavaValue[] elements;
	private int dimensions;
	private String componentType;
	private List<T> elementsModel;

	ArrayModel(IJavaArray array, RuntimeModel runtime) throws DebugException {
		super(array, runtime);
		int len = Math.min(array.getLength(), PandionJView.getMaxArrayLength());
		elementsModel = new ArrayList<T>(len);
		elements = array.getValues();
		dimensions = getDimensions(array);
		componentType = getComponentType(array);
		//		prev = getValues();
		initArray(array, len);
	}

	private void initArray(IJavaArray array, int length) throws DebugException {
		for(int i = 0; i < length - 1; i++)
			elementsModel.add(createElement((IJavaVariable) array.getVariable(i), i));

		if(length > 0)
			elementsModel.add(createElement((IJavaVariable) array.getVariable(array.getLength()-1), array.getLength()-1));
	}

	abstract T createElement(IJavaVariable var, int index) throws DebugException;

	public T getElementModel(int index) {
		if(index >= 0 && index < elementsModel.size()-1)
			return elementsModel.get(index);
		else if(getLength() > 0 && index == getLength()-1)
			return elementsModel.get(elementsModel.size()-1);
		else {
			assert false;
			return null;
		}
	}

	public Iterator<Integer> getValidModelIndexes() {
		return new Iterator<Integer>() {
			int i = 0;
			@Override
			public boolean hasNext() {
				return isValidModelIndex(i);
			}

			@Override
			public Integer next() {
				int r = i;
				i++;
				if(i == elementsModel.size() - 1)
					i = getLength()-1;
				return r;
			}
		};
	}

	@Override
	public boolean isValidModelIndex(int i) {
		return i >= 0 && i < elementsModel.size() - 1 || getLength() > 0 && i == getLength()-1;
	}

	@Override
	public void setStep(int stepPointer) {
		for(T val : elementsModel)
			val.setStep(stepPointer);
	}

	public Object getValues() {
		return getValues(getContent());
	}

	private Object getValues(IJavaArray javaArray) {
		try {
			String compType = getComponentType(javaArray);
			int dimensions = getDimensions(javaArray);
			PrimitiveType primitive = PrimitiveType.match(compType);

			IJavaValue[] values = javaArray.getValues();
			Object array = null;
			if(primitive != null) {
				Class<?> type = primitive.getArrayClass(dimensions); //getDimensions(javaArray) == 2 ? int[].class : int.class;
				array = Array.newInstance(type, values.length);
			}
			else
				array = Array.newInstance(Object.class, values.length);

			if(getDimensions(javaArray) == 1) {
				if(primitive == null) {
					for(int j = 0; j < values.length; j++)
						if(!values[j].isNull())
							Array.set(array, j, values[j].getValueString());
				}
				else {
					primitive.fillPrimitiveValues(array, values);
				}
			}	
			else {
				for(int i = 0; i < values.length; i++) {
					IJavaValue val = values[i];
					if(!val.isNull())
						Array.set(array, i, getValues((IJavaArray) val));
				}
			}
			return array;
		}
		catch (DebugException e) {
			e.printStackTrace();
			getRuntimeModel().setTerminated();
			return new Object[0];
		}
	}

	//	public Object getMatrixValues() {
	//		Class<?> type = Object.class;
	//		PrimitiveType pType = PrimitiveType.match(componentType);
	//		try {
	//			type = Class.forName(componentType);
	//		} catch (ClassNotFoundException e) {
	//		}
	//
	//		try {
	//			int[] dims = matrixDims();
	//			Object array = Array.newInstance(type, dims);
	//			IJavaArray javaArray = getContent();
	//			IJavaValue[] values = javaArray.getValues();
	//			for(int i = 0; i < dims[0]; i++) {
	//				Object line = Array.get(array, i);
	//
	//				if(dims.length == 2) {
	//					IJavaArray aLine = (IJavaArray) values[i];
	//					for(int j = 0; j < dims[1]; j++) {
	//						if(pType == null) {
	//							
	//						}
	//						else {
	//							Array.set(line, j, pType.getValue(aLine.getValue(j)));
	//						}
	//					}
	//				}
	//			}
	//			return array;
	//		}
	//		catch(DebugException e) {
	//			e.printStackTrace();
	//		}
	//		return null;
	//	}
	//
	//	private int[] matrixDims() throws DebugException {
	//		int[] dims = new int[getDimensions()];
	//		IJavaArray javaArray = getContent();
	//		IJavaValue[] values = javaArray.getValues();
	//		dims[0] = values.length;
	//		dims[1] = ((IJavaArray) values[0]).getLength();
	//		
	//		//		Object[] values = getValues();
	//		//		for(Object o : values)
	//		//			if(o == null)
	//		//				return false;
	//		//
	//		//		for(int i = 0; i < values.length-1; i++) {
	//		//			if(((Object[]) values[i]).length != ((Object[]) values[i+1]).length)
	//		//				return false;
	//		//		}
	//
	//		return dims;
	//	}


	//	private Object[] prev;

	//	private static boolean diff(Object[] a, Object[] b, int dim) {
	//		if(a.length == b.length) {
	//			for(int i = 0; i < a.length; i++) {
	//				if(a[i] != null && b[i] != null && a[i].equals(b[i])) {
	//					if(dim > 1 && diff((Object[]) a, (Object[]) b, dim - 1))
	//						return true;
	//				}
	//				else if(a[i] != b[i])
	//					return true;
	//			}
	//			return false;
	//		}
	//		return true;
	//	}



	//	private static boolean diff2(IJavaValue[] a, IJavaValue[] b, int dim) {
	//		try {
	//			if(a.length == b.length) {
	//				for(int i = 0; i < a.length; i++) {
	//					if(!a[i].isNull() && !b[i].isNull() && a[i].equals(b[i])) {
	//						if(dim > 1 && diff2(((IJavaArray) a[i]).getValues(), ((IJavaArray) b[i]).getValues(), dim - 1))
	//							return true;
	//					}
	//					else if(!a[i].equals(b[i]))
	//						return true;
	//				}
	//				return false;
	//			}
	//		}
	//		catch(DebugException e) {
	//			e.printStackTrace();
	//		}
	//		return true;
	//	}

	public final boolean update(int step) {

		// updates values if primitive, updates only references otherwise
		for(T e : elementsModel)
			e.update(step);


		//			if(e.update(step))
		//				setChanged();

		//		if(!hasChanged() && getDimensions() > 1) {
		//			
		//			
		//			Object newValues = getValues2();
		//			if(!Arrays.deepEquals(prev, (Object[]) newValues))
		//				setChanged();
		//				
		////			Object[] newValues = getValues(getContent());
		//			if(diff(prev, newValues, getDimensions()))
		//			prev = newValues;
		//		}
		//		else
		//			prev = getValues(getContent());

		//		if(!hasChanged() && getDimensions() > 1) {
		//			Object[] newValues = getValues(getContent());
		//			if(diff(prev, newValues, getDimensions()))
		//				setChanged();
		//			prev = newValues;
		//		}
		//		else
		//			prev = getValues(getContent());

		//		boolean hasChanged = hasChanged();
		//		notifyObservers(prev);
		
		setChanged();
		notifyObservers();
		return true;
	}

	//	abstract boolean updateInternal(int i, int step);

	public int getLength() {
		return elements.length;
	}

	public int getDimensions() {
		return dimensions;
	}

	public String getElementString(int i) {
		return elements[i].toString();
	}


	private static int getDimensions(IJavaArray array)  throws DebugException  {
		String sig = array.getJavaType().getSignature();
		return Signature.getArrayCount(sig);
	}

	public String getComponentType() {
		return componentType;
	}

	@Override
	public boolean isNull() {
		return false;
	}

	static String getComponentType(IJavaArray array) throws DebugException {
		String sig = array.getJavaType().getSignature();
		return Signature.getSignatureSimpleName(Signature.getElementType(sig));
	}


	@Override
	public boolean isMatrix() {
		if(dimensions < 2)
			return false;

		try {
			for(int i = 0; i < elements.length-1; i++) {
				if(elements[i].isNull() || elements[i+1].isNull())
					return false;
				else
					if(((IJavaArray) elements[i]).getLength() != ((IJavaArray) elements[i+1]).getLength())
						return false;
			}
		} catch (DebugException e) {
			e.printStackTrace(); // TODO termination
		}

		return true;
	}



	public Dimension getMatrixDimension() {
		if(!isMatrix())
			throw new IllegalStateException("not a matrix");

		try {
			return new Dimension(getLength() == 0 ? 0 : ((IJavaArray) elements[0]).getLength(), getLength());
		} catch (DebugException e) {
			e.printStackTrace();
			getRuntimeModel().setTerminated();
		}
		return new Dimension(0, 0);
	}

	public List<T> getModelElements() {
		return Collections.unmodifiableList(elementsModel);
	}

	@Override
	public String toString() {
		int lim = Math.min(PandionJView.getMaxArrayLength(), elements.length);
		String els = "{";
		for(int i = 0; i < lim; i++) {
			if(i != 0)
				els += ", ";
			els += getElementString(i);
		}
		if(lim < elements.length)
			els += ", ...";

		if(els.length() == 1)
			els += " ";

		els += "}";
		return els;
	}
}
