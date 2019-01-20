package model.machine;

import model.program.IDataType;

public interface IValue {
	// TODO value overflow error
	IDataType getType();
	Object getValue();
	
	default boolean isNull() {
		return this == NULL;
	}
	
	default int getMemory() {
		return getType().getMemoryBytes();
	}
	
	IValue NULL = new IValue() {
		@Override
		public IDataType getType() {
			return null;
		}

		@Override
		public Object getValue() {
			return null;
		}
		
		@Override
		public String toString() {
			return "null";
		}
	};
	
	IValue TRUE = new IValue() {
		
		@Override
		public Object getValue() {
			return true;
		}
		
		@Override
		public IDataType getType() {
			return IDataType.BOOLEAN;
		}
		
		@Override
		public String toString() {
			return "true";
		}
	};
	
	IValue FALSE = new IValue() {
		
		@Override
		public Object getValue() {
			return false;
		}
		
		@Override
		public IDataType getType() {
			return IDataType.BOOLEAN;
		}
		
		@Override
		public String toString() {
			return "false";
		}
	};
	
	
	
	static IValue booleanValue(boolean value) {
		return value ? IValue.TRUE : IValue.FALSE;
	}
}
