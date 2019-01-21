package model.program.semantics;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import model.program.IBlock;
import model.program.IProgramElement;

public interface ISemanticChecker {

	String getName();
	Collection<Class<? extends ISemanticRule<?>>> getRules();
	
	default <T> Collection<ISemanticRule<T>> getRules(Class<T> type) {
		return (Collection<ISemanticRule<T>>) getRules().stream()
				.filter(r -> r.getClass().isAssignableFrom(type))
				.map(r -> {
					try {
						return r.newInstance();
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				})
				.collect(Collectors.toList());
	}
}
