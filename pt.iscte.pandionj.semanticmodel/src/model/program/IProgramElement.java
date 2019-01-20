package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

public interface IProgramElement {
	void setProperty(String key, Object value);
	
	Object getProperty(String key);
	
	default List<ISemanticProblem> validateSematics() {
		return ImmutableList.of();
	}
	
	// get specific source element ID?
}
