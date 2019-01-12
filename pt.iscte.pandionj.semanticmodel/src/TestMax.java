import model.machine.IExecutionData;
import model.machine.impl.ProgramState;
import model.program.IBinaryExpression;
import model.program.IBinaryOperator;
import model.program.IBlock;
import model.program.IDataType;
import model.program.IFactory;
import model.program.IProcedure;
import model.program.IProgram;
import model.program.IVariableDeclaration;
import model.program.impl.Factory;

public class TestMax {
	public static void main(String[] args) {
		IFactory factory = new Factory();
		IProgram program = factory.createProgram();
		
		IProcedure maxFunc = program.createProcedure("max", IDataType.INT);
		IVariableDeclaration aParam = maxFunc.addParameter("a", IDataType.INT);
		IVariableDeclaration bParam = maxFunc.addParameter("b", IDataType.INT);
		
		IBinaryExpression e = factory.binaryExpression(IBinaryOperator.GREATER, aParam.expression(), bParam.expression());
		IBlock ifblock = maxFunc.block();
		ifblock.returnStatement(aParam.expression());
		IBlock elseblock = maxFunc.block();
		elseblock.returnStatement(bParam.expression());
		maxFunc.selection(e, ifblock, elseblock);
		
		
		IProcedure main = program.createProcedure("main", IDataType.INT);
//		program.setMainProcedure(main);
		IVariableDeclaration mVar = main.variableDeclaration("m", IDataType.INT);
		mVar.assignment(maxFunc.call(factory.literal(2), factory.literal(1)));

		ProgramState state = new ProgramState(program);
		
//		state.enterInteractiveMode();
		IExecutionData data = state.execute(maxFunc, "2","-1");
		System.out.println(data);
	}

}
