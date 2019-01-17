package tests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	TestArithmeticExpressions.class,
	TestBooleanFuncions.class,
	TestRandom.class,
	TestSelection.class,
	TestSum.class,
	TestArrays.class,
	Test2DArrays.class,
	TestConstants.class,
	TestStruct.class
})

public class JunitTestSuite {   
}  