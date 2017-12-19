package pt.iscte.pandionj.extensibility;

public interface IObservableModel<T> {
	default void registerObserver(ModelObserver<T> o) { }
	default void registerDisplayObserver(ModelObserver<T> o) { }
	default void unregisterObserver(ModelObserver<T> o) { }
}
