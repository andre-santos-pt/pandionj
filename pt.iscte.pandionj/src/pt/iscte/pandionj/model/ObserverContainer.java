package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.List;

import pt.iscte.pandionj.extensibility.ModelObserver;

class ObserverContainer<A> {
	private final List<ModelObserver<A>> obs;
	private boolean changed;

	public ObserverContainer() {
		obs = new ArrayList<>();
		changed = false;
	}

	public void addObserver(ModelObserver<A> o) {
		if (o == null)
			throw new NullPointerException();
		if (!obs.contains(o))
			obs.add(o);
	}

	public void deleteObserver(ModelObserver<A> o) {
		obs.remove(o);
	}

	public void notifyObservers() {
		notifyObservers(null);
	}

	public void notifyObservers(A arg) {
		if (!changed)
			return;
		
		for(ModelObserver<A> o : new ArrayList<>(obs))
			o.update(arg);

		clearChanged();
	}

	public void deleteObservers() {
		obs.clear();
	}

	public void setChanged() {
		changed = true;
	}

	public void clearChanged() {
		changed = false;
	}

	public boolean hasChanged() {
		return changed;
	}

	public int countObservers() {
		return obs.size();
	}
	
	
}
