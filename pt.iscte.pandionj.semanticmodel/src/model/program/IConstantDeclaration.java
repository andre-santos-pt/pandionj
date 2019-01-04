package model.program;

public interface IConstantDeclaration extends IVariableDeclaration {
	@Override
	default IProcedure getProcedure() {
		return null;
	}

	IProgram getProgram();
	
	Object getValue();
}
