package impl.program;

import model.program.ISourceElement;

abstract class SourceElement implements ISourceElement {
	private String source;
	private int offset;
	private int length;
	private int line;
		
	public void setLocation(String source, int offset, int length, int line) {
		this.source = source;
		this.offset = offset;
		this.length = length;
		this.line = line;
	}

	@Override
	public String getSourceCode() {
		return source;
	}
	
	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public int getLine() {
		return line;
	}

}
