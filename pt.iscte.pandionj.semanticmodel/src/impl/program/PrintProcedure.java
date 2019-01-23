package impl.program;

import java.util.List;

import model.machine.IValue;
import model.program.IDataType;

// TODO
public class PrintProcedure extends BuiltinProcedure {

	public PrintProcedure() {
		super("print", IDataType.INT);
		addParameter("value", IDataType.INT);
	}

	@Override
	IValue hookAction(List<IValue> arguments) {
		System.out.println(arguments.get(0));
		return IValue.NULL;
	}
	
	
	//	@Override
	//	public boolean execute(ICallStack stack) throws ExecutionError {
	//		IValue value = stack.getTopFrame().getVariable("value");
	//		System.out.println(value.getValue());
	//		return true;
	//	}
}
