package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.extensibility.ITag;
import pt.iscte.pandionj.extensibility.IVariableModel;

public abstract class VariableModel<T extends IJavaValue>
extends ModelElement<T,IVariableModel.VariableEvent<?>> 
implements IVariableModel {
	
	protected final IJavaVariable variable;
	private StackFrameModel stackFrame;  // optional (if owned by variable)

	private String type;
	private String name;
	private boolean isInstance;
	private boolean isStatic;
	private boolean isVisible;
	
	private List<T> history;

	private ITag tag;



	public VariableModel(IJavaVariable variable, boolean isInstance, boolean isVisible, StackFrameModel stackFrame) throws DebugException {
		this(variable, isInstance, isVisible, stackFrame.getRuntime());
		this.stackFrame = stackFrame;	
	}

	@SuppressWarnings("unchecked")
	public VariableModel(IJavaVariable variable, boolean isInstance, boolean isVisible, RuntimeModel runtime) throws DebugException {
		super(runtime);
		assert variable != null;

		T val = (T) variable.getValue();
		this.variable = variable;
		this.name = variable.getName();
		
		if(!((IJavaValue) val).isNull())
			this.type = variable.getReferenceTypeName();
		this.isInstance = isInstance; // !variable.isLocal() ?
		this.isVisible = isVisible;
		this.isStatic = variable.isStatic();
		history = new ArrayList<>();
		history.add(val);
	}

	public StackFrameModel getStackFrame() {
		return stackFrame;
	}

	public void setOutOfScope() {
		setChanged();
		notifyObservers(new IVariableModel.VariableEvent<T>(IVariableModel.VariableEvent.Type.OUT_OF_SCOPE, null));
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean update(int step) {
		try {
			T current = history.get(history.size()-1);
			boolean equals = variable.getValue().equals(current);
			if(!equals) {
				T val = (T) variable.getValue();
				history.add(val);
				setChanged();
				notifyObservers(new IVariableModel.VariableEvent<T>(IVariableModel.VariableEvent.Type.VALUE_CHANGE, val));
				return true;
			}
		}
		catch(DebugException e) {
			e.printStackTrace(); // TODO terminate
		}
		return false;
	}

	public String getName() {		
		return name;
	}

	public boolean isInstance() {
		return isInstance;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public boolean isDecimal() {
		return false;
	}

	public boolean isBoolean() {
		return false;
	}
	
	@Override
	public boolean isVisible() {
		return isVisible;
	}

	public String getTypeName() {
		return type;
	}


	@Override
	public T getContent() {
		return history.get(history.size()-1);
	}

	public String getCurrentValue() {
		return getContent().toString();
	}

	public List<String> getHistory() {
		List<String> hist = new ArrayList<>();
		for(T v : history)
			hist.add(v.toString());
		return hist;
	}

	@Override
	public IJavaVariable getJavaVariable() {
		return variable;
	}

	public void setTag(ITag tag) {	
		this.tag = tag;
	}

	public ITag getTag() {
		return tag;
	}
}
