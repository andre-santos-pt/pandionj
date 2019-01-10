package model.program;

import model.machine.IValue;

// TODO
public interface IConstantDeclaration extends IVariableDeclaration {
	@Override
	default IProcedure getProcedure() {
		return null;
	}

	IProgram getProgram();
	
	IValue getValue();
}
