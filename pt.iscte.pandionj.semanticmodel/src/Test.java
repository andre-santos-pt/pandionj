import model.machine.impl.ProgramState;
import model.program.IDataType;
import model.program.IFactory;
import model.program.IProblem;
import model.program.IProcedure;
import model.program.IProcedureCallExpression;
import model.program.IProgram;
import model.program.IVariableAssignment;
import model.program.IVariableDeclaration;
import model.program.impl.Factory;

public class Test {
	
	public static void main(String[] args) {
		IFactory factory = new Factory();

		IProgram program = factory.createProgram();
		
		IProcedure proc = program.createProcedure("inc", IDataType.DOUBLE);
//		IVariableDeclaration nParam = proc.addParameter("n", IDataType.INT);
		
		IProcedureCallExpression randomCall = program.getProcedure("random").callExpression();
		
		proc.returnStatement(randomCall);
		
//		IVariableDeclaration rVar = proc.variableDeclaration("r", program.getDataType("double"));
//		IVariableAssignment rAss = rVar.assignment(randomCall);
		
//		ILiteral lit = factory.literal(4);
//		IBinaryExpression e = factory.binaryExpression(IOperator.ADD, rVar.expression(), lit);
		

//		IVariableAssignment ass2 = rVar.assignment(e);
//		proc.returnStatement(rVar.expression());
		
		IProcedure main = program.createProcedure("main", IDataType.VOID);
		IVariableDeclaration var2 = main.variableDeclaration("b", IDataType.INT);	
		IVariableAssignment ass3 = var2.assignment(proc.callExpression(factory.literal(2)));
		
		
		for (IProblem iProblem : program.validate()) {
			System.out.println(iProblem);
		}	
		
		ProgramState state = new ProgramState(program);
		
		state.execute(proc);

	}

}
