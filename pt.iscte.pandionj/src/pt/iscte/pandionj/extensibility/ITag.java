package pt.iscte.pandionj.extensibility;

import java.util.Map;

public interface ITag extends IPropertyProvider {

	String getName();
	
	Map<String,String> getArgs();
}
