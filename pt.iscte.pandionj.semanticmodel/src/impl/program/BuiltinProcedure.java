package impl.program;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;

import impl.machine.Value;
import model.machine.IValue;
import model.program.IDataType;

// TODO different types
public class BuiltinProcedure extends Procedure {

	private Method method;
	
	public BuiltinProcedure(Method method) {
		super(method.getName(), IDataType.INT);
		assert isValidForBuiltin(method);
		this.method = method;
		for (Parameter p : method.getParameters()) {
			addParameter(p.getName(), IDataType.INT);
		}
	}

	public static boolean isValidForBuiltin(Method method) {
		if(!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers()) || !method.getReturnType().equals(int.class))
			return false;
		
		for (Class<?> ptype : method.getParameterTypes()) {
			if(!ptype.equals(int.class))
				return false;
		}
		return true;
	}
	
	public IValue hookAction(List<IValue> arguments) {
		Object[] args = new Object[arguments.size()];
		for(int i = 0; i < arguments.size(); i++)
			args[i] = ((Number) arguments.get(i).getValue()).intValue();
		try {
			Object ret = method.invoke(null, args);
			return Value.create(getReturnType(), ret);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
}
