package model.program;

public interface IConstantDeclaration extends ISourceElement, IIdentifiableElement {

	IProgram getProgram();
	
	IDataType getType();
	
	ILiteral getValue();
	
	IConstantExpression expression();
}
