package pt.iscte.pandionj.extensibility;

import java.util.Observer;

public interface IArrayModel extends IEntityModel {
	int getLength();
	int getDimensions();
	String getComponentType();
	Object[] getValues();
	boolean isMatrix();
	void registerObserver(Observer o);
	void registerDisplayObserver(Observer o);
	void unregisterObserver(Observer o);
	// TODO observer event
}
