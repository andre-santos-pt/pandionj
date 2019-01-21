package model.program.semantics.java;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import model.program.IModule;
import model.program.semantics.ISemanticChecker;
import model.program.semantics.ISemanticRule;

public class JavaSemanticChecker implements ISemanticChecker {

	public JavaSemanticChecker(IModule program) {
	}

	@Override
	public String getName() {
		return "Java semantic checker";
	}

	@Override
	public Collection<Class<? extends ISemanticRule<?>>> getRules() {
		return null;
	}
}
