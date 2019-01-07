import com.google.common.collect.ImmutableList;

import model.machine.impl.ProgramState;
import model.program.IBinaryExpression;
import model.program.IBlock;
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
import model.program.impl.Return;

public class Test {
	public static void main(String[] args) {
		IFactory factory = new Factory();

		IProgram program = factory.createProgram();
		
		IProcedure proc = factory.createProcedure("sumTwo", ImmutableList.of());
		program.addProcedure(proc);
		
		IVariableDeclaration var = factory.createVariableDeclaration(proc, "a", program.getDataType("int"));	
		ILiteral lit = factory.createLiteral("2");
		IVariableAssignment ass = factory.createAssignment(var, lit);
		
		IBinaryExpression e = factory.createBinaryExpression(Operator.ADD, factory.createVariableExpression(var), lit);
		

		IVariableAssignment ass2 = factory.createAssignment(var, e);

		IReturn ret = new Return(factory.createVariableExpression(var));
	
		IBlock seq = factory.createBlock(proc, var, ass, ass2, ret);
		proc.setBody(seq);

		
		IProcedure main = factory.createProcedure("main", ImmutableList.of());
		IProcedureCall call = factory.createProcedureCall(proc, ImmutableList.of());
		IVariableDeclaration var2 = factory.createVariableDeclaration(proc, "b", program.getDataType("int"));	
		IVariableAssignment ass3 = factory.createAssignment(var2, call);

		
		IBlock mainBlock = factory.createBlock(main, var2, ass3);
		main.setBody(mainBlock);
		
		program.addProcedure(main);
		program.setMainProcedure(main);
		
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
