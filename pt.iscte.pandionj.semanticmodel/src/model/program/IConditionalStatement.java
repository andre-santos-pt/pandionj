package model.program;

public interface IConditionalStatement extends IStatement {
	IExpression getGuard(); // not null
	IBlock getBlock(); // not null
}
