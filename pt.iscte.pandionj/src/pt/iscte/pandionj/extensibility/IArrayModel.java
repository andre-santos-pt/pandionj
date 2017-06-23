package pt.iscte.pandionj.extensibility;

import java.util.Collection;

public interface IArrayModel extends IObservableModel {
	int getLength();
	int getDimensions();
	String getComponentType();
	Object[] getValues();
	boolean isMatrix();
	boolean isDecimal();
	IVariableModel getElementModel(int index);
	Collection<IVariableModel> getVars();
}
