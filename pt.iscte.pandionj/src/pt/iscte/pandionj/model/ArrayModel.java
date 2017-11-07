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
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.Constants;
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
		prev = getValues();
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

	public Object[] getValues() {
		return getValues(getContent());
	}

	// TODO remove try catch
	private static Object[] getValues(IJavaArray javaArray) {
		try {
			String compType = getComponentType(javaArray);
//			boolean isPrimitive = getDimensions(javaArray) == 1 && PrimitiveType.isPrimitive(compType);
//			IJavaType compType = ((IJavaArrayType) javaArray.getJavaType()).getComponentType();
			IJavaValue[] values = javaArray.getValues();
			Object[] array = new Object[javaArray.getLength()];
			
			if(getDimensions(javaArray) == 1) {
				PrimitiveType primitive = PrimitiveType.match(compType);
				if(primitive == null) {
					for(int j = 0; j < values.length; j++)
						array[j] = values[j].isNull() ? null : values[j].getValueString();
				}
				else {
					primitive.fillPrimitiveWrapperValues(javaArray.getValues(), array);
				}
			}	
			else {
				for(int i = 0; i < array.length; i++) {
					IJavaValue val = values[i];
					if(!val.isNull())
						array[i] = getValues((IJavaArray) val); // TODO erro cast
				}
			}
			return array;
		}
		catch (DebugException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Object getMatrixValues() {
		Class<?> type = Object.class;
		PrimitiveType pType = PrimitiveType.match(componentType);
		try {
			type = Class.forName(componentType);
		} catch (ClassNotFoundException e) {
		}

		try {
			int[] dims = matrixDims();
			Object array = Array.newInstance(type, dims);
			IJavaArray javaArray = getContent();
			IJavaValue[] values = javaArray.getValues();
			for(int i = 0; i < dims[0]; i++) {
				Object line = Array.get(array, i);

				if(dims.length == 2) {
					IJavaArray aLine = (IJavaArray) values[i];
					for(int j = 0; j < dims[1]; j++) {
						if(pType == null) {
							// TODO
						}
						else {
							Array.set(line, j, pType.getValue(aLine.getValue(j)));
						}
					}
				}
			}
			return array;
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
		return null;
	}

	private int[] matrixDims() throws DebugException {
		int[] dims = new int[getDimensions()];
		IJavaArray javaArray = getContent();
		IJavaValue[] values = javaArray.getValues();
		dims[0] = values.length;
		dims[1] = ((IJavaArray) values[0]).getLength();
		
		//		Object[] values = getValues();
		//		for(Object o : values)
		//			if(o == null)
		//				return false;
		//
		//		for(int i = 0; i < values.length-1; i++) {
		//			if(((Object[]) values[i]).length != ((Object[]) values[i+1]).length)
		//				return false;
		//		}

		return dims;
	}


	private Object[] prev;

	private static boolean diff(Object[] a, Object[] b, int dim) {
		if(a.length == b.length) {
			for(int i = 0; i < a.length; i++) {
				if(a[i] != null && b[i] != null && a[i].equals(b[i])) {
					if(dim > 1 && diff((Object[]) a, (Object[]) b, dim - 1))
						return true;
				}
				else if(a[i] != b[i])
					return true;
			}
			return false;
		}
		return true;
	}

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
		for(T e : elementsModel)
			if(e.update(step))
				setChanged();

		//		int len = Math.min(getLength(), Constants.ARRAY_LENGTH_LIMIT);
		//		for(int i = 0; i < len; i++)	
		//			if(updateInternal(i, step))
		//				setChanged();

		if(!hasChanged() && getDimensions() > 1) {
			Object[] newValues = getValues(getContent());
			if(diff(prev, newValues, getDimensions()))
				setChanged();
			prev = newValues;
		}
		else
			prev = getValues(getContent());

		boolean hasChanged = hasChanged();
		notifyObservers(prev);
		return hasChanged;
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

		Object[] values = getValues();
		for(Object o : values)
			if(o == null)
				return false;

		for(int i = 0; i < values.length-1; i++) {
			if(((Object[]) values[i]).length != ((Object[]) values[i+1]).length)
				return false;
		}

		return true;
	}

	

	public Dimension getMatrixDimension() throws DebugException {
		if(!isMatrix())
			throw new IllegalStateException("not a matrix");

		//		try {
		return new Dimension(getLength(), getLength() == 0 ? 0 : ((IJavaArray) elements[0]).getLength());
		//		} catch (DebugException e) {
		//			e.printStackTrace();
		//			return null;
		//		}
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
