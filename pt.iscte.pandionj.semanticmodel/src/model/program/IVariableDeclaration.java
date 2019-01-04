package model.program;

public interface IVariableDeclaration extends IStatement, IIdentifiableElement {
	IProcedure getProcedure();
	
	IDataType getType();
	
	int getArrayDimensions();

	boolean isReference();
	
	default boolean isArray() {
		return getArrayDimensions() > 0;
	}
	
	/*
	boolean isConstant();
	default IVariableRole getRole() {
		return IVariableRole.NONE;
	}
	*/
	
	class UnboundVariable implements IVariableDeclaration {
		
		final String name;
		
		UnboundVariable(String name) {
			this.name = name;
		}
		
		@Override
		public String getSourceCode() {
			return null;
		}

		@Override
		public int getOffset() {
			return -1;
		}

		@Override
		public int getLength() {
			return -1;
		}

		@Override
		public int getLine() {
			return -1;
		}

		@Override
		public String getIdentifier() {
			return name;
		}

		@Override
		public IProcedure getProcedure() {
			return null;
		}

		@Override
		public IDataType getType() {
			return null;
		}

		@Override
		public int getArrayDimensions() {
			return -1;
		}

		@Override
		public boolean isReference() {
			return false;
		}
	}
}
