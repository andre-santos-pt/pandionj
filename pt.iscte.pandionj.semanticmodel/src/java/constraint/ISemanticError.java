package java.constraint;

public interface ISemanticError {

	String getReason();
	
	interface ReturnMissing extends ISemanticError {
		
	}
	
}
