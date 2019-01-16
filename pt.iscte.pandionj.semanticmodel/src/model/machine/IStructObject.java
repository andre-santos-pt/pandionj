package model.machine;

public interface IStructObject extends IValue {

	IValue getField(String id);
	void setField(String id, IValue value);
}
