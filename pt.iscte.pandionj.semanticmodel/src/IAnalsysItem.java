import java.util.List;

import model.program.IProgramElement;

public interface IAnalsysItem {
	String getTitle();
	String getDescription();
	List<IProgramElement> getElements();
}
