package pt.iscte.pandionj.parser;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class ClassInfo {
	
	private String name;
	private VisibilityInfo visibility;
	private List<MethodInfo> methods;
	
	public ClassInfo(String name, VisibilityInfo visibility) {
		assert name != null && !name.isEmpty();
		assert visibility != null;
		
		this.name = name;
		this.visibility = visibility;
		methods = new ArrayList<>();
	}
	
	public void addMethod(MethodInfo m) {
		methods.add(m);
		
	}
	
	public Iterator<MethodInfo> getMethods(EnumSet<VisibilityInfo> visibility) {
		return methods.stream().filter((m) -> visibility.contains(m.getVisibility())).iterator();
	}

	public MethodInfo getMethod(String name) {
		for(MethodInfo m : methods)
			if(m.getName().equals(name))
				return m;
		return null;
	}
	
}
