package tests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	TestArithmeticExpressions.class,
	TestBooleanFuncions.class,
	TestSelection.class,
	TestSum.class,
})

public class JunitTestSuite {   
}  