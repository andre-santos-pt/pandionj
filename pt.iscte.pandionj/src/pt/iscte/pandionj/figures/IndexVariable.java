package pt.iscte.pandionj.figures;

import org.eclipse.swt.graphics.Color;

import pt.iscte.pandionj.extensibility.IVariableModel;

class IndexVariable {
	enum Direction {
		NONE, FORWARD, BACKWARD;
	}
	
	final IVariableModel model;
	final Color color;

	int constBound;
	IVariableModel varBound;
	
	boolean isBar;
	boolean markError;

	
	IndexVariable(IVariableModel model, Color color) {
		this.model = model;
		this.color = color;
		constBound = -1;
		varBound = null;
	}
			
	IndexVariable(IVariableModel model, Color color, int constBound) {
		this(model, color);
		this.constBound = constBound;
	}

	IndexVariable(IVariableModel model, Color color, IVariableModel varBound) {
		this(model, color);
		this.varBound = varBound;
	}
	
	int getCurrentIndex() {
		return Integer.parseInt(model.getCurrentValue());
	}

	boolean isBounded() {
		return constBound != -1 || varBound != null;
	}

	int getBound() {
		if(!isBounded())
			return -1;
		else
			return constBound != -1 ? constBound : Integer.parseInt(varBound.getCurrentValue());
	}

	Direction getDirection() {
		if(isBounded())
			return getCurrentIndex() < getBound() ? Direction.FORWARD : Direction.BACKWARD;
		else	
			return Direction.NONE;
	}

	boolean isBar() {
		return isBar;
	}

	void markError() {
		markError = true;
	}
}