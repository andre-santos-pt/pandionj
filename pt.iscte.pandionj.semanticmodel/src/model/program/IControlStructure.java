package model.program;

public interface IControlStructure extends IBlock {
	IBlock getParent();
	IExpression getGuard(); 
}
