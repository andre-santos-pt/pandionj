package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

public interface ISemanticProblem {
	enum Type {
		
		INCOMPATIBLE_ASSIGNMENT,
		INCOMPATIBLE_RETURN,
		MISSING_RETURN;
	}
	
	List<ISourceElement> getSourceElements();
	String getMessage();
	
	
	static ISemanticProblem create(String message, ISourceElement ... elements) {
		return new SemanticProblem(message, elements);
	}
	
	
	class SemanticProblem implements ISemanticProblem {
		private final String message;
		private final ImmutableList<ISourceElement> elements;
		
		private SemanticProblem(String message, ISourceElement ... elements) {
			this.message = message;
			this.elements = ImmutableList.copyOf(elements);
		}
		
		@Override
		public List<ISourceElement> getSourceElements() {
			return elements;
		}
		
		@Override
		public String getMessage() {
			return message;
		}
		
		@Override
		public String toString() {
			return elements + " : " + message; 
		}
	}
}
