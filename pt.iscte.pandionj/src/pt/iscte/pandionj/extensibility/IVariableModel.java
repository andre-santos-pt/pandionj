package pt.iscte.pandionj.extensibility;

import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.parser.VariableInfo;

public interface IVariableModel<O> extends IObservableModel<O> {
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
	void setOutOfScope();
	boolean update(int step);
	IJavaVariable getJavaVariable();
	void setStep(int stepPointer);
	void setVariableRole(VariableInfo info);
	
	IRuntimeModel getRuntimeModel();
}
