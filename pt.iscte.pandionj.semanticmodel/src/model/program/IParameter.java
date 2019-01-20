package model.program;

// TODO ideia
interface IParameter extends IVariableDeclaration {
	default IExpression getPrecondition() {
		return null; 
	}
}
