package pt.iscte.pandionj.model;

import java.util.Observable;

public abstract class ArrayModel extends ModelElement {

//	private Class<?> type;
//	
//	public ArrayModel(IJavaArray array) {
//		
//	}
	
	public abstract int getLength();
	public abstract int getDimensions();
	public abstract String getComponentType();
	public abstract boolean isPrimitiveType();
	public abstract Object[] getValues();
	
	
}
