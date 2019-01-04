package model.program;

public interface IArrayAssignment extends IVariableAssignment {
	IExpression[] getIndexes(); // not null, length >= 1
	default int getDimensions() {
		return getIndexes().length;
	}
}
