package pt.iscte.pandionj.extensibility;

public interface IObjectModel extends IEntityModel {

	int getInt(String fieldName);
	IArrayModel getArray(String fieldName);
	String getStringValue();
}
