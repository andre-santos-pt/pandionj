package model.program.semantics.java;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import model.program.semantics.ISemanticChecker;
import model.program.semantics.Rule;

public class JavaSemanticChecker implements ISemanticChecker {

	@Override
	public String getName() {
		return "Java semantic checker";
	}

	@Override
	public Collection<Class<? extends Rule>> getRules() {
		return ImmutableList.of(
				Identifiers.class
				);
	}
}
