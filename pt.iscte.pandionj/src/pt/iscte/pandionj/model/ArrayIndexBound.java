package pt.iscte.pandionj.model;

import java.util.Map;
import java.util.WeakHashMap;

import pt.iscte.pandionj.PandionJView;
import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IArrayIndexModel.BoundType;
import pt.iscte.pandionj.extensibility.IObjectModel.InvocationResult;

// TODO dangling refs
class ArrayIndexBound extends DisplayUpdateObservable<Integer> implements IArrayIndexModel.IBound {
	String expression;
	IArrayIndexModel.BoundType type;
	Integer value;
	RuntimeModel runtime;
	
	static Map<String, Integer> expressionCache = new WeakHashMap<>();
	
	public ArrayIndexBound(String expression, IArrayIndexModel.BoundType type, RuntimeModel runtime) {
		assert expression != null;
		assert type != null;
		this.runtime = runtime;
		this.expression = expression;
		this.type = type;
		this.value = expressionCache.get(expression);
		eval();
	}

	private void eval() {
		runtime.evaluate(expression, new InvocationResult() {
			@Override
			public void valueReturn(Object o) {
				try {
					if(o == null)
						return;
					
					Integer newVal = Integer.valueOf(o.toString());
					if(!newVal.equals(value)) {
						value = newVal;
						expressionCache.put(expression, value);
						setChanged(); 
						notifyObservers(value);
					}
				}
				catch(NumberFormatException e) {
					value = null;
					setChanged(); 
					notifyObservers(value);
				}
			}
		});

	}

	@Override
	public String toString() {
		return "< " + expression + " -> " + getValue() + " " + type + ">";
	}

	@Override
	public String getExpression() {
		return expression;
	}

	@Override
	public Integer getValue() {
		return value;
	}

	@Override
	public BoundType getType() {
		return type;
	}
}