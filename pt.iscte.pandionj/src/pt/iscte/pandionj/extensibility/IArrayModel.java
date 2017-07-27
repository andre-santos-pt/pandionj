package pt.iscte.pandionj.extensibility;

public interface IArrayModel extends IObservableModel {
	int getLength();
	int getDimensions();
	String getComponentType();
	Object[] getValues();
	boolean isMatrix();
	boolean isDecimal();
	IVariableModel getElementModel(int index);
}
