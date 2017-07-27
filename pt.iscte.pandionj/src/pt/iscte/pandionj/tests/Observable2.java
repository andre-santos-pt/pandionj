package pt.iscte.pandionj.tests;

import java.util.ArrayList;
import java.util.List;

public class Observable2<A> {
	private final List<Observer2<A>> obs;
	private boolean changed;

	public Observable2() {
		obs = new ArrayList<>();
		changed = false;
	}

	public void addObserver(Observer2<A> o) {
		if (o == null)
			throw new NullPointerException();
		if (!obs.contains(o)) {
			obs.add(o);
		}
	}

	public void deleteObserver(Observer2<A> o) {
		obs.remove(o);
	}

	public void notifyObservers() {
		notifyObservers(null);
	}

	public void notifyObservers(A arg) {
		if (!changed)
			return;

		for(Observer2<A> o : obs)
			o.update(this, arg);

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
