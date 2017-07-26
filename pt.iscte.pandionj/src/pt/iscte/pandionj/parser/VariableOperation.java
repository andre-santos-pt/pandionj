package pt.iscte.pandionj.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VariableOperation {
	public enum Type {
		CONST,
		INDEX(2),
		ACCESS(2),
		INC,
		DEC,
		BOUNDED(1), 
		SUBS,
		ACC(1),
		IN_ASSIGN(1);

		private int nParams;

		private Type() {
			this(0);
		}

		private Type(int nParams) {
			this.nParams = nParams;
		}

		public boolean hasParams() {
			return nParams != 0;
		}
		
		public boolean isModifier() {
			return this == INC || this == DEC || this == SUBS || this == ACC;
		}
		
		public boolean isStepper() {
			return this == INC || this == DEC;
		}
	}

	private final String varName;
	private final Type type;
	private final List<Object> paramValues;

	public VariableOperation(String varName, Type type, Object ... paramValues) {
		this.varName = varName;
		this.type = type;
		this.paramValues = paramValues.length == 0 ? Collections.emptyList() : new ArrayList<>(type.nParams);
		for(Object p : paramValues)
			this.paramValues.add(p);
	}

	@Override
	public String toString() {
		return type.name() + (type.hasParams() ? ":" + paramValues : "");
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.getClass().equals(VariableOperation.class) && toString().equals(obj.toString());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	public String getVarName() {
		return varName;
	}

	public Type getType() {
		return type;
	}

	public boolean isModifier() {
		return type.isModifier();
	}
	
	public boolean isStepper() {
		return type.isStepper();
	}
	
	

	public Object getParam(int index) {
		return index < paramValues.size() ? paramValues.get(index) : null;
	}


}
