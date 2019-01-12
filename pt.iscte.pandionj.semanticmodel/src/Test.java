import model.machine.impl.ProgramState;
import model.program.IBinaryExpression;
import model.program.IDataType;
import model.program.IFactory;
import model.program.ILiteral;
import model.program.IOperator;
import model.program.IProcedure;
import model.program.IProcedureCall;
import model.program.IProgram;
import model.program.IVariableAssignment;
import model.program.IVariableDeclaration;
import model.program.impl.Factory;

public class Test {
	public static void main(String[] args) {
		IFactory factory = new Factory();

		IProgram program = factory.createProgram();
		
		IProcedure proc = program.createProcedure("inc", IDataType.INT);
		IVariableDeclaration nParam = proc.addParameter("n", IDataType.INT);
		
		IVariableDeclaration rVar = proc.variableDeclaration("r", program.getDataType("int"));
		IVariableAssignment rAss = rVar.assignment(nParam.expression());
		
		ILiteral lit = factory.literal(4);
		IBinaryExpression e = factory.binaryExpression(IOperator.ADD, rVar.expression(), lit);
		

		IVariableAssignment ass2 = rVar.assignment(e);
		proc.returnStatement(rVar.expression());
		
		IProcedure main = program.createProcedure("main", IDataType.VOID);
//		program.setMainProcedure(main);
		IProcedureCall call = factory.procedureCall(proc, factory.literal(2));
		IVariableDeclaration var2 = main.variableDeclaration("b", IDataType.INT);	
		IVariableAssignment ass3 = var2.assignment(call);
		
		
		ProgramState state = new ProgramState(program);
		
		state.execute(main);

	}

}
