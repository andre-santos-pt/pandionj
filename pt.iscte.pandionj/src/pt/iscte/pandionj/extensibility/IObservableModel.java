package pt.iscte.pandionj.extensibility;

import pt.iscte.pandionj.tests.Observer2;

public interface IObservableModel<T> {
	default void registerObserver(Observer2<T> o) { }
	default void registerDisplayObserver(Observer2<T> o) { }
	default void unregisterObserver(Observer2<T> o) { }
}
