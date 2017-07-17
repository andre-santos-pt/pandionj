package pt.iscte.pandionj.model;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.widgets.Display;

import pt.iscte.pandionj.extensibility.IObservableModel;

public class DisplayUpdateObservable extends Observable implements IObservableModel {

	public void registerObserver(Observer o) {
		addObserver(o);
	}

	public void registerDisplayObserver(Observer obs) {
		addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				Display.getDefault().asyncExec(() -> {
					obs.update(o, arg);
				});
			}
		});
	}
	
	public void unregisterObserver(Observer o) {
		deleteObserver(o);
	}

}