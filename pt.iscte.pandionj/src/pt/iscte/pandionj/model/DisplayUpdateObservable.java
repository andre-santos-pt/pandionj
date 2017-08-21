package pt.iscte.pandionj.model;

import org.eclipse.swt.widgets.Display;

import pt.iscte.pandionj.extensibility.IObservableModel;
import pt.iscte.pandionj.tests.Observable2;
import pt.iscte.pandionj.tests.Observer2;

public class DisplayUpdateObservable<T> implements IObservableModel<T> {
	
	private Observable2<T> obs = new Observable2<>();
	
	public void registerObserver(Observer2<T> o) {
		obs.addObserver(o);
	}

	public void registerDisplayObserver(Observer2<T> o) {
		obs.addObserver(new Observer2<T>() {
			public void update(Observable2<T> observable, T arg) {
				Display.getDefault().asyncExec(() -> {
					o.update(observable, arg);
				});
			}
		});
	}
	
	public void unregisterObserver(Observer2<T> o) {
		obs.deleteObserver(o);
	}
	
	public void setChanged() {
		obs.setChanged();
	}
	
	public boolean hasChanged() {
		return obs.hasChanged();
	}
	
	public void notifyObservers() {
		obs.notifyObservers();
	}
	
	public void notifyObservers(T arg) {
		obs.notifyObservers(arg);
	}
	
	

}
