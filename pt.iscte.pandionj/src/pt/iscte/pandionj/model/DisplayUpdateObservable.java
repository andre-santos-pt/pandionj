package pt.iscte.pandionj.model;

import org.eclipse.swt.widgets.Display;

import pt.iscte.pandionj.extensibility.IObservableModel;

public class DisplayUpdateObservable<T> implements IObservableModel<T> {
	
	private ObserverContainer<T> obs = new ObserverContainer<>();
	
	public void registerObserver(ModelObserver<T> o) {
		obs.addObserver(o);
	}

	public void registerDisplayObserver(ModelObserver<T> o) {
		obs.addObserver(new ModelObserver<T>() {
			public void update(T arg) {
				if(Thread.currentThread() != Display.getDefault().getThread())
					Display.getDefault().asyncExec(() -> {
						o.update(arg);
					});
				else {
					o.update(arg);
				}
			}
		});
	}
	
	public void unregisterObserver(ModelObserver<T> o) {
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
