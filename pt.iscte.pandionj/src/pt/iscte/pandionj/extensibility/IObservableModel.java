package pt.iscte.pandionj.extensibility;

import java.util.Observer;

public interface IObservableModel {
	default void registerObserver(Observer o) { }
	default void registerDisplayObserver(Observer o) { }
	default void unregisterObserver(Observer o) { }
}
