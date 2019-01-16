package java.constraint;

public interface ISemanticConstraint {

	String getReason();
	
	interface ReturnMissing extends ISemanticConstraint {
		
	}
	
}
