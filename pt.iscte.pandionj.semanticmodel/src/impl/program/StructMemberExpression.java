package impl.program;

import model.program.IDataType;
import model.program.IStructType;
import model.program.IStructMemberExpression;

class StructMemberExpression extends SourceElement implements IStructMemberExpression {

	private final IStructType struct;
	private final String memberId;
	
	public StructMemberExpression(IStructType struct, String memberId) {
		assert struct != null;
		assert memberId != null;
		
		this.struct = struct;
		this.memberId = memberId;
	}

	@Override
	public IStructType getStruct() {
		return struct;
	}

	@Override
	public String getMemberId() {
		return memberId;
	}

	@Override
	public IDataType getType() {
		return struct;
	}
	

}
