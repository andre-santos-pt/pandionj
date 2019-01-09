import model.machine.impl.ProgramState;
import model.program.IBinaryExpression;
import model.program.IBlock;
import model.program.IDataType;
import model.program.IFactory;
import model.program.IProcedure;
import model.program.IProgram;
import model.program.IVariableDeclaration;
import model.program.Operator;
import model.program.impl.Factory;

public class TestMax {
	public static void main(String[] args) {
		IFactory factory = new Factory();
		IProgram program = factory.createProgram();
		
		IProcedure maxFunc = program.createProcedure("max", IDataType.INT);
		IVariableDeclaration aParam = maxFunc.addParameter("a", IDataType.INT);
		IVariableDeclaration bParam = maxFunc.addParameter("b", IDataType.INT);
		
		IBinaryExpression e = factory.createBinaryExpression(Operator.GREATER, aParam.expression(), bParam.expression());
		IBlock ifblock = maxFunc.createBlock();
		ifblock.createReturn(aParam.expression());
		IBlock elseblock = maxFunc.createBlock();
		elseblock.createReturn(bParam.expression());
		maxFunc.createSelection(e, ifblock, elseblock);
		
		IProcedure main = program.createProcedure("main", IDataType.INT);
		program.setMainProcedure(main);
		IVariableDeclaration mVar = main.createVariableDeclaration("m", IDataType.INT);
		mVar.assignment(maxFunc.call(factory.value(2), factory.value(1)));

		
		ProgramState state = new ProgramState(program);
		state.execute();
	}

}
