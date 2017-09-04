package pt.iscte.pandionj.extensibility;

public interface IEntityModel extends IObservableModel<Object> {

	boolean isNull();
	IRuntimeModel getRuntimeModel();
}
