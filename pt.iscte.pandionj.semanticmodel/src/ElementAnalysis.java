import java.util.Collection;

import model.program.IProgramElement;

public interface ElementAnalysis<T extends IProgramElement> {

	Collection<IAnalsysItem> perform(T e);
}
