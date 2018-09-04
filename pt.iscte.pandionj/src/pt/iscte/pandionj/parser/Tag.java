package pt.iscte.pandionj.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pt.iscte.pandionj.extensibility.ITag;

 class Tag implements ITag {

	private final String name;
	private final Map<String,String> args;
	
	public Tag(String name, Map<String,String> args) {
		if(!name.matches("[a-z]+"))
			throw new IllegalArgumentException("invalid tag name: " + name);
		
		this.name = name;
		this.args = new HashMap<String, String>(args);
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public Map<String,String> getArgs() {
		return Collections.unmodifiableMap(args);
	}
	
	@Override
	public Set<String> getPropertyNames() {
		return Collections.unmodifiableSet(args.keySet());
	}

	@Override
	public String getProperty(String name) {
		return args.get(name);
	}
	
	@Override
	public boolean hasProperty(String name) {
		return args.containsKey(name);
	}

	@Override
	public String toString() {
		if(args.isEmpty())
			return "@" + name;
		else
			return "@" + name + "(" + String.join(", ", args.keySet()) + ")";
	}
}
