package model.program.semantics;

import java.util.Collection;

public interface ISemanticChecker {

	String getName();
	Collection<Class<? extends Rule>> getRules();
	
//	<T extends IProgramElement> Collection<ISemanticRule<T>> getRules(Class<T> type);
	
//	default <T> Collection<ISemanticRule<T>> getRules(Class<T> type) {
//		return (Collection<ISemanticRule<T>>) getRules().stream()
//				.filter(r -> r.getClass().isAssignableFrom(type))
//				.map(r -> {
//					try {
//						return r.newInstance();
//					} catch (Exception e) {
//						e.printStackTrace();
//						return null;
//					}
//				})
//				.collect(Collectors.toList());
//	}
}
