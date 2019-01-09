import model.machine.impl.ProgramState;
import model.program.IBinaryExpression;
import model.program.IBlock;
import model.program.IDataType;
import model.program.IFactory;
import model.program.ILiteral;
import model.program.IProcedure;
import model.program.IProcedureCall;
import model.program.IProgram;
import model.program.IReturn;
import model.program.IVariableAssignment;
import model.program.IVariableDeclaration;
import model.program.Operator;
import model.program.impl.Factory;

public class Test {
	public static void main(String[] args) {
		IFactory factory = new Factory();

		IProgram program = factory.createProgram();
		
		IProcedure proc = program.createProcedure("inc", IDataType.INT);
		IVariableDeclaration nParam = proc.addParameter("n", IDataType.INT);
		
		IVariableDeclaration rVar = proc.createVariableDeclaration("r", program.getDataType("int"));
		IVariableAssignment rAss = rVar.assignment(nParam.expression());
		
		ILiteral lit = factory.value(4);
		IBinaryExpression e = factory.createBinaryExpression(Operator.ADD, 
				rVar.expression(), lit);
		

		IVariableAssignment ass2 = rVar.assignment(e);
		proc.createReturn(rVar.expression());
		
		IProcedure main = program.createProcedure("main", IDataType.VOID);
		program.setMainProcedure(main);
		IProcedureCall call = factory.createProcedureCall(proc, factory.value(2));
		IVariableDeclaration var2 = main.createVariableDeclaration("b", IDataType.INT);	
		IVariableAssignment ass3 = var2.assignment(call);
		
		
//		Output out = IFactory.createOutput();
//		out.setExpression(var);
		
//		IVariableExpression aVar = factory.createVariableExpression(var);
//		ILiteral lit3 = factory.createLiteral("2");
//		IBinaryExpression e = factory.createBinaryExpression(Operator.EQUAL, aVar, lit3);
		
//		ISelection iff = factory.createSelection(e, factory.createBlock(out));
//		iffblock.getStatements().add(out);
		
//		seq.getStatements().add(iff);
		
//		System.out.println(program.toCode());
		
//		IProcedure printProc = new PrintProcedure();
		
		ProgramState state = new ProgramState(program);
		
		state.execute();

	}

}
