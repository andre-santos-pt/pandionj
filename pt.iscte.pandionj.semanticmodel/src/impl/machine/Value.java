package impl.machine;

import model.machine.IValue;
import model.program.IDataType;

public final class Value implements IValue {
	private final IDataType type;
	private final Object value;
	
	private Value(IDataType type, Object value) {
		assert value != null;
		assert !(type.equals(IDataType.BOOLEAN)); // enforce flyweight
		this.type = type;
		this.value = value;
	}

	public static IValue create(IDataType type, Object value) {
		if(type.equals(IDataType.BOOLEAN)) {
			assert value instanceof Boolean;
			return (boolean) value ? IValue.TRUE : IValue.FALSE;
		}
		else if(value == null) // TODO also for non primitive types
			return IValue.NULL;
		else
			return new Value(type, type.create(value.toString()));
	}
	@Override
	public IDataType getType() {
		return type;
	}

	@Override
	public Object getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return value.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		return value.equals(obj);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
}
