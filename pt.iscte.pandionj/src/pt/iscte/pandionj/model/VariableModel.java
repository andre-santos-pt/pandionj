package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.extensibility.IVariableModel;

public abstract class VariableModel<T extends IJavaValue, O>
extends ModelElement<T,O> 
implements IVariableModel<O> {

	protected final IJavaVariable variable;
	private StackFrameModel stackFrame;  // optional (if owned by variable)

	private String type;
	private String name;
	private boolean isInstance;
	private boolean isStatic;
	private boolean isVisible;
	
	private List<StepValue> history;

	private int stepInit;
	private int scopeEnd;

	private int stepPointer;
	private Collection<String> tags = Collections.emptyList();


	private class StepValue {
		final T value;
		final int step;

		StepValue(T value, int step) {
			this.value = value;
			this.step = step;
		}

		@Override
		public String toString() {
			return "(" + value + ", " + step + ")";
		}
	}

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
		StepValue sv = new StepValue(val, runtime.getRunningStep());
		history.add(sv);
		stepPointer = 0;
		this.stepInit = runtime.getRunningStep();
		this.scopeEnd = Integer.MAX_VALUE;
	}

	public StackFrameModel getStackFrame() {
		return stackFrame;
	}

	//	public boolean isWithinScope() {
	//		if(stackFrame == null)
	//			return true;
	//		else
	//			return stepInit <= stackFrame.getStepPointer() && stackFrame.getStepPointer() <= scopeEnd;
	//	}

	public void setOutOfScope() {
		this.scopeEnd = getRuntimeModel().getRunningStep();
		setChanged();
		notifyObservers();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean update(int step) {
		try { // TODO ObjectCollectedException?
			StepValue current = history.get(history.size()-1); // FIXME
			boolean equals = variable.getValue().equals(current.value);
			if(!equals) {
				StepValue sv = new StepValue((T) variable.getValue(), step);
				history.add(sv);
				stepPointer++;
				setChanged();
				notifyObservers();
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

	public void setStep(int step) {
		int temp = stepPointer;
		while(stepPointer != history.size() - 1 && history.get(stepPointer).step < step)
			stepPointer++;

		while(stepPointer != 0 && history.get(stepPointer).step > step)
			stepPointer--;

		assert stepPointer >= 0 && stepPointer < history.size();
		if(stepPointer != temp) {
			setChanged();
			notifyObservers();
		}
	}



	@Override
	public T getContent() {
		return history.get(stepPointer).value;
	}

	public String getCurrentValue() {
		return history.get(stepPointer).value.toString();
	}

	public List<String> getHistory() {
		List<String> hist = new ArrayList<>();
		for(StepValue sv : history)
			hist.add(sv.value.toString());
		return hist;
	}

	@Override
	public IJavaVariable getJavaVariable() {
		return variable;
	}

	public void setTags(Collection<String> tags) {	
		if(this.tags == Collections.EMPTY_LIST)
			this.tags = new ArrayList<String>(tags.size());
	
		this.tags.addAll(tags);
	}

	public Collection<String> getTags() {
		return Collections.unmodifiableCollection(tags);
	}

	public boolean hasTags() {
		return !tags.isEmpty();
	}
}
