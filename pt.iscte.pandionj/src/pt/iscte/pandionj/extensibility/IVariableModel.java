package pt.iscte.pandionj.extensibility;

import pt.iscte.pandionj.parser.VariableInfo;

public interface IVariableModel extends IObservableModel {
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

	boolean isWithinScope();
	
	Role getRole();
	
	VariableInfo getVariableRole();
	
	boolean isStatic();
}
