package model.program;

public interface IArrayType extends IDataType {
	int getDimensions();
	IDataType getComponentType();
}
