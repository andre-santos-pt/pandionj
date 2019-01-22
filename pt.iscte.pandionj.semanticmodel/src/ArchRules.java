import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import model.program.IBinaryOperator;
import model.program.IExpression;
import model.program.IModule;
import model.program.IOperator;
import model.program.IStatement;
import model.program.IUnaryOperator;

public class ArchRules {

	public static Collection<Class<?>> properSubTypes(Class<?> superType, String basePackage) throws IOException {
		ClassPath cp = ClassPath.from(ClassLoader.getSystemClassLoader());
		List<Class<?>> list = new ArrayList<Class<?>>();
		for (ClassInfo classInfo : cp.getTopLevelClassesRecursive(basePackage)) {
			Class<?> c = classInfo.load();
			if(c != superType && superType.isAssignableFrom(c))
				list.add(c);
		}
		return list;
	}
	
	public static void main(String[] args) throws IOException {
		String basePackage = "model";
		
		Collection<Class<?>> statements = properSubTypes(IStatement.class, basePackage);
		System.out.println("Statements: " + IStatement.class.getName());
		statements.forEach(s -> System.out.println("\t" + s.getName()));
		System.out.println("total: " + statements.size());

		System.out.println();
		
		Collection<Class<?>> expressions = properSubTypes(IExpression.class, basePackage);
		System.out.println("Expressions: " + IExpression.class.getName());
		expressions.forEach(e -> System.out.println("\t" + e.getName()));
		System.out.println("total: " + expressions.size());
		
		System.out.println();

		Collection<Class<?>> uoperators = properSubTypes(IUnaryOperator.class, basePackage);
		System.out.println("Unary operators: " + IUnaryOperator.class.getName());
		uoperators.forEach(o -> printEnum(o));

		System.out.println();
		
		Collection<Class<?>> boperators = properSubTypes(IBinaryOperator.class, basePackage);
		System.out.println("Binary operators: " + IBinaryOperator.class.getName());
		boperators.forEach(o -> printEnum(o));
	}

	private static void printEnum(Class<?> o) {
		if(o.isEnum()) {
			for(Field f : o.getFields())
				if(f.isEnumConstant())
					System.out.println("\t" + f.getName() + "\t\t\t(" + o.getSimpleName() + ")");
		}
		else
			System.out.println("\t" + o.getName());
	}
}
