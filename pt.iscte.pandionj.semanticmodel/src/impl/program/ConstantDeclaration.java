package impl.program;

import model.program.IConstantDeclaration;
import model.program.IConstantExpression;
import model.program.IDataType;
import model.program.ILiteral;
import model.program.IProgram;

class ConstantDeclaration extends ProgramElement implements IConstantDeclaration {
	private final IProgram program;
	private final String id;
	private final IDataType type;
	private final ILiteral value;
	
	public ConstantDeclaration(IProgram program, String id, IDataType type, ILiteral value) {
		this.program = program;
		this.id = id;
		this.type = type;
		this.value = value;
	}

	@Override
	public IProgram getProgram() {
		return program;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public IDataType getType() {
		return type;
	}

	@Override
	public ILiteral getValue() {
		return value;
	}
	
	@Override
	public IConstantExpression expression() {
		return new ConstantExpression(this);
	}
	
	@Override
	public String toString() {
		return type + " " + id + " = " + value;
	}

}
