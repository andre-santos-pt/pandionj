package tests;

import org.junit.BeforeClass;
import org.junit.Test;

import impl.machine.ProgramState;
import model.program.IConstantDeclaration;
import model.program.IDataType;
import model.program.IFactory;
import model.program.IOperator;
import model.program.IProcedure;
import model.program.IProgram;
import model.program.IVariableDeclaration;

public class TestConstants {

	static IFactory fact = IFactory.INSTANCE;
	static IProgram program;
	private static IConstantDeclaration piConst;
	
	@BeforeClass
	public static void setup() {
		program = fact.createProgram();	
		piConst = program.addConstant("PI", IDataType.DOUBLE, fact.literal(3.14159265359));
	}
	
	@Test
	public void testArea() {
		
		IProcedure circleArea = program.addProcedure("circleArea", IDataType.DOUBLE);
		IVariableDeclaration rParam = circleArea.addParameter("r", IDataType.DOUBLE);
		circleArea.getBody().addReturnStatement(fact.binaryExpression(IOperator.MUL, fact.binaryExpression(IOperator.MUL, piConst.expression(), rParam.expression()), rParam.expression()));
		
		ProgramState state = new ProgramState(program);
		state.execute(circleArea, 3);
		
	}
	
	@Test
	public void testPerimeter() {
		
		IProcedure circlePerimeter = program.addProcedure("circlePerimeter", IDataType.DOUBLE);
		IVariableDeclaration rParam = circlePerimeter.addParameter("r", IDataType.DOUBLE);
		circlePerimeter.getBody().addReturnStatement(fact.binaryExpression(IOperator.MUL, fact.binaryExpression(IOperator.MUL, fact.literal(2), piConst.expression()), rParam.expression()));
		ProgramState state = new ProgramState(program);
		state.execute(circlePerimeter, 3);

		System.out.println(program);
	}
}
