package model.program;

public interface IControlStructure extends IProgramElement {
	IBlock getParent();
	IExpression getGuard(); 
}
