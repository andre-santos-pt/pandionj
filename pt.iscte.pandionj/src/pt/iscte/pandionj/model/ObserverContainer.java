package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.List;

class ObserverContainer<A> {
	private final List<ModelObserver<A>> obs;
	private boolean changed;

	public ObserverContainer() {
		obs = new ArrayList<>();
		changed = false;
	}

	public synchronized void addObserver(ModelObserver<A> o) {
		if (o == null)
			throw new NullPointerException();
		if (!obs.contains(o)) {
			obs.add(o);
		}
	}

	public synchronized void deleteObserver(ModelObserver<A> o) {
		obs.remove(o);
	}

	public synchronized void notifyObservers() {
		notifyObservers(null);
	}

	public synchronized void notifyObservers(A arg) {
		if (!changed)
			return;

		for(ModelObserver<A> o : obs)
			o.update(arg);

		clearChanged();
	}

	public synchronized void deleteObservers() {
		obs.clear();
	}

	public synchronized void setChanged() {
		changed = true;
	}

	public synchronized void clearChanged() {
		changed = false;
	}

	public synchronized boolean hasChanged() {
		return changed;
	}

	public synchronized int countObservers() {
		return obs.size();
	}
	
	
}
