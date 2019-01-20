package model.program;

public interface IConstantDeclaration extends IProgramElement, IIdentifiableElement {

	IProgram getProgram();
	
	IDataType getType();
	
	ILiteral getValue();
	
	IConstantExpression expression();
}
