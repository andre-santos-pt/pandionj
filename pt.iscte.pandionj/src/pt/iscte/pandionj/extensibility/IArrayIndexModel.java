package pt.iscte.pandionj.extensibility;

public interface IArrayIndexModel extends IVariableModel {
	enum Direction {
		NONE, FORWARD, BACKWARD;
	}

	enum BoundType {
		OPEN, CLOSE;
	}
	
	interface IBound {
		int getValue();
		BoundType getType();
		String getExpression();
	}
	
	IVariableModel getArrayReference();
	
	int getCurrentIndex();
	
	Direction getDirection();
	
	IBound getBound();	
}
