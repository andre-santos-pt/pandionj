package pt.iscte.pandionj.extensibility;

public interface IArrayIndexModel extends IValueModel {
	enum BoundType {
		OPEN, CLOSE;
	}
	
	interface IBound {
		Integer getValue();
		BoundType getType();
		String getExpression();
	}
	
	IVariableModel getArrayReference();
	
	int getCurrentIndex();
	
	Direction getDirection();
	
	IBound getBound();

}
