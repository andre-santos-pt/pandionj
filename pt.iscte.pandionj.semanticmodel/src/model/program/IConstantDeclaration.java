package model.program;

public interface IConstantDeclaration extends IIdentifiableElement {

	IProgram getProgram();
	
	IDataType getType();
	
	ILiteral getValue();
	
	IConstantExpression expression();
}
