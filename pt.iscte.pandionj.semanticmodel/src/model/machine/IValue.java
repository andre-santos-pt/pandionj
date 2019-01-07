package model.machine;

import model.program.IDataType;

public interface IValue {
	IDataType getDataType();
	Object getValue();
	
	IValue NULL_VALUE = new IValue() {
		@Override
		public IDataType getDataType() {
			return null;
		}

		@Override
		public Object getValue() {
			return null;
		}
	};
}
