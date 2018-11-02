package pt.iscte.pandionj.extensibility;

import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.parser.VariableInfo;

public interface IVariableModel extends IObservableModel<IVariableModel.VariableEvent<?>> {
	
	class VariableEvent<T> {
		public enum Type {
			VALUE_CHANGE, OUT_OF_SCOPE;
		}
		public final Type type;
		public final T arg;

		public VariableEvent(Type type, T arg) {
			this.type = type;
			this.arg = arg;
		}
	}
	
	public enum Role {
		FIXED_VALUE {
			public String toString() { return "Fixed Value";}
		},
		ARRAY_ITERATOR {
			public String toString() { return "Array Index Iterator";}
		},
		FIXED_ARRAY_INDEX {
			public String toString() { return "Fixed Array Index";}
		},
		GATHERER {
			public String toString() { return "Gatherer";}
		},
		STEPPER {
			public String toString() { return "Stepper";}
		},
		MOST_WANTED_HOLDER {
			public String toString() { return "Most-Wanted Holder";}
		},
		NONE {
			public String toString() { return "";}
		};
		
		public boolean isArrayAccessor() {
			return this == ARRAY_ITERATOR || this == FIXED_ARRAY_INDEX; 
		}
	}
	
	String getName();
	String getTypeName();
	
	
	boolean isInstance();
	
	Role getRole();
	VariableInfo getVariableRole();
	
	boolean isStatic();
	boolean isVisible();
	void setOutOfScope(); // TODO remove?
	boolean update(int step);
	IJavaVariable getJavaVariable();
	void setVariableRole(VariableInfo info);
	
	IRuntimeModel getRuntimeModel();
	
	ITag getTag();
	
	default boolean hasTag() {
		return getTag() != null;
	}
	void setTag(ITag tag);
}
