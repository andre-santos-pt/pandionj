package model.machine;

import model.program.IDataType;

public interface IValue {
	IDataType getDataType();
	Object getValue();
	
	IValue NULL = new IValue() {
		@Override
		public IDataType getDataType() {
			return null;
		}

		@Override
		public Object getValue() {
			return null;
		}
		
		@Override
		public String toString() {
			return "NULL";
		}
	};
	
	IValue TRUE = new IValue() {
		
		@Override
		public Object getValue() {
			return true;
		}
		
		@Override
		public IDataType getDataType() {
			return IDataType.BOOLEAN;
		}
	};
	
	IValue FALSE = new IValue() {
		
		@Override
		public Object getValue() {
			return false;
		}
		
		@Override
		public IDataType getDataType() {
			return IDataType.BOOLEAN;
		}
	};
}
