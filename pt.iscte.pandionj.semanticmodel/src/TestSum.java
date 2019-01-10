import model.machine.impl.ProgramState;
import model.program.IArrayType;
import model.program.IArrayVariableDeclaration;
import model.program.IDataType;
import model.program.IFactory;
import model.program.IOperator;
import model.program.IProcedure;
import model.program.IProgram;
import model.program.IVariableDeclaration;
import model.program.impl.Factory;

public class TestSum {

	public static void main(String[] args) {
		IFactory factory = new Factory();
		IProgram program = factory.createProgram();
		
		IProcedure f = program.createProcedure("sum", IDataType.INT);
		IArrayVariableDeclaration vParam = (IArrayVariableDeclaration) f.addParameter("v", new IArrayType.ValueTypeArray(IDataType.INT, 1));
		
		
		IVariableDeclaration sVar = f.variableDeclaration("s", IDataType.INT);
		sVar.assignment(factory.literal(1));
		sVar.assignment(factory.binaryExpression(IOperator.ADD, sVar.expression(), vParam.elementExpression(factory.literal(1))));
		f.returnStatement(sVar.expression());
		
		IProcedure main = program.createProcedure("main", IDataType.INT);
		program.setMainProcedure(main);
		
		
		IArrayVariableDeclaration array = main.arrayDeclaration("test", IDataType.INT, 1);
		array.assignment(factory.arrayAllocation(IDataType.INT, factory.literal(3)));
		array.elementAssignment(factory.literal(5), factory.literal(0));
		array.elementAssignment(factory.literal(7), factory.literal(1));
		array.elementAssignment(factory.literal(9), factory.literal(2));
		
		IVariableDeclaration mVar = main.variableDeclaration("s", IDataType.INT);
		mVar.assignment(f.call(array.expression()));

		ProgramState state = new ProgramState(program);
		state.execute();
	}

}
