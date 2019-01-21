import java.util.Collection;

import model.program.IProgramElement;
import model.program.semantics.ISemanticProblem;

public interface ElementAnalysis<T extends IProgramElement> {

	Collection<IAnalsysItem> perform(T e);
}
