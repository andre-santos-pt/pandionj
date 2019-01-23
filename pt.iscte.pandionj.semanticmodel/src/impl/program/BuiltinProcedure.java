package impl.program;

import java.util.List;

import model.machine.IValue;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IProcedure;
import model.program.IProcedureCallExpression;
import model.program.IProcedureDeclaration;
import model.program.IVariableDeclaration;

// TODO
public abstract class BuiltinProcedure extends Procedure {

//	private Procedure procedure;
	
	public BuiltinProcedure(String id, IDataType returnType) {
		super(id, returnType);
	}

	//	@Override
	//	public boolean execute(ICallStack stack) throws ExecutionError {
	//		IValue value = stack.getTopFrame().getVariable("value");
	//		System.out.println(value.getValue());
	//		return true;
	//	}

	abstract IValue hookAction(List<IValue> arguments);
	
//	@Override
//	public String getId() {
//		return procedure.getId();
//	}
//
//	@Override
//	public void setProperty(String key, Object value) {
//		procedure.setProperty(key, value);
//	}
//
//	@Override
//	public Object getProperty(String key) {
//		return procedure.getProperty(key);
//	}
//
//	@Override
//	public List<IVariableDeclaration> getParameters() {
//		return procedure.getParameters();
//	}
//
//	@Override
//	public IDataType getReturnType() {
//		return procedure.getReturnType();
//	}
//
//	@Override
//	public IVariableDeclaration addParameter(String id, IDataType type) {
//		return procedure.addParameter(id, type);
//	}
//
//	@Override
//	public IProcedureCallExpression callExpression(List<IExpression> args) {
//		return procedure.callExpression(args);
//	}
}
