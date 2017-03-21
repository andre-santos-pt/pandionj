package pt.iscte.pandionj.extensibility;

import java.util.Observer;

public interface IArrayModel {
	int getLength();
	int getDimensions();
	String getComponentType();
	void registerObserver(Observer o);
}
