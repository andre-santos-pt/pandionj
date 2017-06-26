package pt.iscte.pandionj.extensibility;

public interface IArrayIndexModel extends IVariableModel {
	enum Direction {
		NONE, FORWARD, BACKWARD;
	}

	int getCurrentIndex();
	
	Direction getDirection();
	
	boolean isBounded();

	int getBound();
}
