package pt.iscte.pandionj.model;

public interface ModelObserver<A> {

	void update(ObserverContainer<A> obs, A arg);

}
