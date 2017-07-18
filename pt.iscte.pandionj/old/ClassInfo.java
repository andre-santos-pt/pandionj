package pt.iscte.pandionj.parser2;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/**
 * This is my class
 * @author andresantos
 *
 */
public class ClassInfo {
	private String name; // T1
	
	/* img */
	private VisibilityInfo visibility; // T2
	private List<FieldInfo> fields;
	private List<MethodInfo> methods;
	
	public ClassInfo(String name, VisibilityInfo visibility) {
		assert name != null && !name.isEmpty();
		assert visibility != null;
		
		this.name = name;
		this.visibility = visibility;
		fields = new ArrayList<>();
		methods = new ArrayList<>();
	}

	public void addField(FieldInfo f) {
		int[][] img = null; // bwimage
		fields.add(f);
	}
	
	/*
	 * This is a method
	 */
	public void addMethod(MethodInfo m) {
		methods.add(m);
		
	}
	
	/** 
	 * This is another method
	 * @param visibility
	 * @return
	 */
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
