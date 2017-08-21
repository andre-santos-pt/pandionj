package pt.iscte.pandionj.extensibility;

import pt.iscte.pandionj.model.PrimitiveType;

public interface IArrayModel<T> extends IEntityModel {
	int getLength();
	int getDimensions();
	String getComponentType();
	Object[] getValues();
	boolean isMatrix();
	boolean isDecimal();
	T getElementModel(int index);
	default boolean isPrimitive() {
		return getDimensions() == 1 && PrimitiveType.isPrimitive(getComponentType());
	}
}
