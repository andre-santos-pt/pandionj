package model.program.impl;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import model.program.IArrayElementAssignment;
import model.program.IArrayElementExpression;
import model.program.IArrayLengthExpression;
import model.program.IArrayVariableDeclaration;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IVariableRole;

class ArrayVariableDeclaration extends VariableDeclaration implements IArrayVariableDeclaration {
	private final int numberOfDimensions;
	
	public ArrayVariableDeclaration(Block block, String name, IDataType type, int numberOfDimensions, Set<VariableDeclaration.Flag> flags) {
		super(block, name, type, flags);
		this.numberOfDimensions = numberOfDimensions;
	}

	@Override
	public boolean isReference() {
		return true;
	}

	@Override
	public int getArrayDimensions() {
		return numberOfDimensions;
	}

	@Override
	public IArrayLengthExpression lengthExpression(List<IExpression> indexes) {
		return new ArrayLengthExpression(indexes);
	}
	
	@Override
	public IArrayElementExpression elementExpression(List<IExpression> indexes) {
		return new ArrayElementExpression(this, indexes);
	}
	
	@Override
	public IArrayElementAssignment elementAssignment(IExpression expression, List<IExpression> indexes) {
		return new ArrayElementAssignment(getParent(), this, indexes, expression);
	}
	
	public IVariableRole getRole() {
		return IVariableRole.NONE;
	}
	
	@Override
	public String toString() {
		return (isReference() ? "*var " : "var ") + getIdentifier() + " (" + getType() + ")";
	}
	
	private class ArrayLengthExpression extends SourceElement implements IArrayLengthExpression {
		private ImmutableList<IExpression> indexes;
		
		public ArrayLengthExpression(List<IExpression> indexes) {
			this.indexes = ImmutableList.copyOf(indexes);
		}
		
		@Override
		public List<IExpression> getIndexes() {
			return indexes;
		}
		
		@Override
		public IArrayVariableDeclaration getVariable() {
			return ArrayVariableDeclaration.this;
		}

		@Override
		public IDataType getType() {
			return IDataType.INT;
		}
		
		@Override
		public String toString() {
			return getVariable().getIdentifier() + ".length";
		}
	}
}
