package impl.program;

import model.program.IStructAllocation;
import model.program.IStructType;

class StructAllocation extends SourceElement implements IStructAllocation {

	private final IStructType type;
	
	public StructAllocation(IStructType type) {
		this.type = type;
	}


	@Override
	public IStructType getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return "new " + getType().getId(); 
	}

}
