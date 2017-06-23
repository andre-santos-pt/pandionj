package pt.iscte.pandionj.extensibility;

import java.util.List;

public interface IVisibleMethod {

	String getName();

	List<String> getParameterTypes();
	
	default int getNumberOfParameters() {
		return getParameterTypes().size();
	}
}
