package impl.program;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import model.program.IProgramElement;

abstract class ProgramElement implements IProgramElement {
	
//	ISourceElement getIdLocation(); //TODO id location

	
//	private String source;
//	private int offset;
//	private int length;
//	private int line;
//		
//	public void setLocation(String source, int offset, int length, int line) {
//		this.source = source;
//		this.offset = offset;
//		this.length = length;
//		this.line = line;
//	}

//	@Override
//	public String getSourceCode() {
//		return source;
//	}
//	
//	@Override
//	public int getOffset() {
//		return offset;
//	}
//
//	@Override
//	public int getLength() {
//		return length;
//	}
//
//	@Override
//	public int getLine() {
//		return line;
//	}
	
	private Map<String, Object> properties = Collections.emptyMap();
	
	@Override
	public Object getProperty(String key) {
		return properties.get(key);
	}
	
	@Override
	public void setProperty(String key, Object value) {
		if(properties.isEmpty())
			properties = new HashMap<>(5);
		
		properties.put(key, value);
	}
}
