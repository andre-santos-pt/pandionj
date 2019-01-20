package impl.program;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import impl.machine.ExecutionError;
import impl.machine.ExecutionError.Type;
import model.machine.IArray;
import model.machine.ICallStack;
import model.machine.IValue;
import model.program.IArrayElementAssignment;
import model.program.IArrayElementExpression;
import model.program.IArrayLengthExpression;
import model.program.IArrayType;
import model.program.IArrayVariableDeclaration;
import model.program.IBlock;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IProgramElement;
import model.program.roles.IVariableRole;

class ArrayVariableDeclaration extends VariableDeclaration implements IArrayVariableDeclaration {
	private final IArrayType arrayType;

	public ArrayVariableDeclaration(Block block, String id, IArrayType type, Set<VariableDeclaration.Flag> flags) {
		super(block, id, type.getComponentType(), flags);
		this.arrayType = type;
	}

	@Override
	public boolean isReference() {
		return true;
	}

	@Override
	public int getArrayDimensions() {
		return arrayType.getDimensions();
	}

	@Override
	public IArrayType getType() {
		return arrayType;
	}

	@Override
	public IDataType getComponentType() {
		return arrayType.getComponentType();
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
		IProgramElement parent = getParent();
		assert parent instanceof IBlock;
		return new ArrayElementAssignment((IBlock) parent, this, indexes, expression);
	}

	public IVariableRole getRole() {
		return IVariableRole.NONE;
	}

	@Override
	public String toString() {
		//		String brackets = "";
		//		for(int i = 0; i < getArrayDimensions(); i++)
		//			brackets += "[]";
		return (isReference() ? "*var " : "var ") + getId() + " (" + getType() + ")";
	}

	private class ArrayLengthExpression extends Expression implements IArrayLengthExpression {
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
			String text = getVariable().getId();
			for(IExpression e : indexes)
				text += "[" + e + "]";
			return text + ".length";
		}


		@Override
		public List<IExpression> decompose() {
			return indexes;
		}
		
		@Override
		public boolean isDecomposable() {
			return true;
		}	
		
		@Override
		public IValue evalutate(List<IValue> values, ICallStack stack) throws ExecutionError {
			assert values.size() == getIndexes().size();
			IArray array = (IArray) stack.getTopFrame().getVariable(getVariable().getId());
			IValue v = array;
			for(int i = 0; i < values.size(); i++) {
				int index = ((Number) values.get(i).getValue()).intValue();
				if(index < 0 || index >= ((IArray) v).getLength())
					throw new ExecutionError(Type.ARRAY_INDEX_BOUNDS, this, Integer.toString(index));
				v = ((IArray) v).getElement(index);
			}
			return stack.getTopFrame().getValue(((IArray) v).getLength());
		}
	}
}
