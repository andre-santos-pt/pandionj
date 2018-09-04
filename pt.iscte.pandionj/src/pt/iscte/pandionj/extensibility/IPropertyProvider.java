package pt.iscte.pandionj.extensibility;

import java.util.Collections;
import java.util.Set;

public interface IPropertyProvider {

	Set<String> getPropertyNames();
	
	String getProperty(String name);
	
	default Integer getIntProperty(String name, Integer defaultValue) {
		String s = getProperty(name);
		if(s == null)
			return defaultValue;
		try {
			return new Integer(s);
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	boolean hasProperty(String name);
	
	static IPropertyProvider NULL_PROPERTY_PROVIDER = new IPropertyProvider() {
		public Set<String> getPropertyNames() 	{ 	return Collections.emptySet();	}
		public String getProperty(String name)	{	return null;	}
		public boolean hasProperty(String name) {	return false;	}
	};
}
