package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.stream.Collectors;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;

import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;

import pt.iscte.pandionj.model.ObjectModel.SiblingVisitor;



public class RuntimeModel extends Observable {
	private ILaunch launch;
	private List<StackFrameModel> callStack;
	private Map<Long, EntityModel<?>> objects;
	private Map<Long, EntityModel<?>> looseObjects;
	private int countActive;
	private boolean terminated;
	
	private int step;

	public RuntimeModel() {
		callStack = new ArrayList<>();
		objects = new HashMap<>();
		looseObjects = new HashMap<>();
		countActive = 0;
		terminated = false;
		step = 0;
	}

	
	public IJavaDebugTarget getDebugTarget() {
		return (IJavaDebugTarget) launch.getDebugTarget();
	}
	
	public void getActiveStackFrame() {

	}
	
	public void update(IJavaThread thread) {
		if(launch != thread.getLaunch()) {
			launch = thread.getLaunch();
			callStack.clear();
			objects.clear();
			looseObjects.clear();
			step = 0;
			terminated = false;
			countActive = 0;
		}
		
		terminated = false;
	
		// TODO ERRO disconnected
		for(EntityModel<?> o : objects.values().toArray(new EntityModel[objects.size()])) {
			if(o instanceof ArrayModel && o.update(step))
				setChanged();
			else if(o instanceof ObjectModel)
				((ObjectModel) o).traverseSiblings(new SiblingVisitor() {
					public void visit(EntityModel<?> object, ObjectModel parent, int index, int depth, String field) {
						if(object != null && object.update(step))
							setChanged();
					}
				});
		}
		
		try {
			handle(thread.getStackFrames());
		} catch (DebugException e) {
			e.printStackTrace();
		}
		
		for(int i = 0; i < countActive; i++)
			callStack.get(i).update();
		
		notifyObservers();
	}
	
	private void handle(IStackFrame[] stackFrames) {
		assert stackFrames != null;

		// TODO erro?
//		for(IStackFrame f : stackFrames) {
//			if(!(f.getLaunch().getSourceLocator().getSourceElement(f) instanceof IFile)) {
//				return 0;
//			}
//		}

		IStackFrame[] revStackFrames = reverse(stackFrames);
		countActive = revStackFrames.length;
		if(isSubStack(revStackFrames)) {
			for(int i = countActive; i < callStack.size(); i++)
				callStack.get(i).setObsolete();
		}
		else if(isStackIncrement(revStackFrames)) {
			for(int i = callStack.size(); i < revStackFrames.length; i++)
				callStack.add(new StackFrameModel(this, (IJavaStackFrame) revStackFrames[i]));
			setChanged();
		}
		else {
			callStack.clear();
			for(int i = 0; i < revStackFrames.length; i++)
				callStack.add(new StackFrameModel(this, (IJavaStackFrame) revStackFrames[i]));
			setChanged();
		}
		notifyObservers();
	}
	
	private boolean isSubStack(IStackFrame[] stackFrames) {
		if(stackFrames.length > callStack.size())
			return false;
		
		for(int i = 0; i < stackFrames.length; i++)
			if(stackFrames[i] != callStack.get(i).getStackFrame())
				return false;
		
		return true;
	}
	
	private boolean isStackIncrement(IStackFrame[] stackFrames) {
		if(stackFrames.length < callStack.size())
			return false;
		
		for(int i = 0; i < callStack.size(); i++)
			if(stackFrames[i] != callStack.get(i).getStackFrame())
				return false;
		
		return true;
	}

	private static IStackFrame[] reverse(IStackFrame[] stackFrames) {
		IStackFrame[] revStackFrames = new IStackFrame[stackFrames.length];
		for(int i = 0; i < revStackFrames.length; i++)
			revStackFrames[i] = stackFrames[stackFrames.length-1-i];
		return revStackFrames;
	}

	public boolean isEmpty() {
		return countActive == 0;
	}

	public StackFrameModel getTopFrame() {
		assert !isEmpty();
		return callStack.get(countActive-1);
	}

	public int getSize() {
		return countActive;
	}

	public List<StackFrameModel> getStackPath() {
		return Collections.unmodifiableList(callStack);
	}

	public List<StackFrameModel> getFilteredStackPath() {
		return callStack.stream().filter((f) -> f.getLineNumber() != -1).collect(Collectors.toList());
	}
	
	public void simulateGC() {
		// TODO GC
//		boolean removals = false;
//		Iterator<Entry<Long, EntityModel<?>>> iterator = objects.entrySet().iterator();
//		while(iterator.hasNext()) {
//			Entry<Long, EntityModel<?>> e = iterator.next();
//			if(!vars.containsValue(e.getValue())) {
//				iterator.remove();
//				removals = true;
//			}
//		}
//		if(removals) {
//			setChanged();
//			notifyObservers(Collections.emptyList());
//		}

	}

//	public void simulateGC() {
//		for(int i = 0; i < countActive; i++)
//			callStack.get(i).simulateGC();
//	}

	public StackFrameModel getFrame(IStackFrame exceptionFrame) {
		for(StackFrameModel s : callStack)
			if(s.getStackFrame() == exceptionFrame)
				return s;
		
		assert false;
		return null;
	}

	public void setTerminated() {
		terminated = true;
	}
	
	public boolean isTerminated() {
		return terminated;
	}

//	public List<VariableModel<?>> getStaticVariables() {
//		return getTopFrame().getStaticVariables();
//	}
	


	public int getRunningStep() {
		return step;
	}
	
	
	public EntityModel<? extends IJavaObject> getObject(IJavaObject obj, boolean loose, StackFrameModel stackFrame) {
		assert !obj.isNull();
		try {
			EntityModel<? extends IJavaObject> e = objects.get(obj.getUniqueId());
			if(e == null) {
				if(obj.getJavaType() instanceof IJavaArrayType) {
					IJavaType componentType = ((IJavaArrayType) obj.getJavaType()).getComponentType();
					if(componentType instanceof IJavaReferenceType)
						e = new ArrayReferenceModel((IJavaArray) obj, this);
					else
						e = new ArrayPrimitiveModel((IJavaArray) obj, this);
				}
				else {
					IType type = null;
					try {
						type = stackFrame.getJavaProject().findType(obj.getJavaType().getName());
					} catch (JavaModelException e1) {
						e1.printStackTrace();
					}
					e = new ObjectModel(obj, type, this);
				}

				if(loose) {
					looseObjects.put(obj.getUniqueId(), e);
				}
				else {
					objects.put(obj.getUniqueId(), e);
				}
				setChanged();
			}
			return e;
		}
		catch(DebugException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Collection<EntityModel<?>> getLooseObjects() {
		return Collections.unmodifiableCollection(looseObjects.values());
	}
	
	
	private boolean sameLocation(Location loc, StackFrameModel frame) {
		try {
			return loc.declaringType().name().equals(frame.getStackFrame().getDeclaringTypeName()) &&
					loc.lineNumber() == frame.getLineNumber();
		} catch (DebugException e) {
			e.printStackTrace();
			return false;
		}
	}
	public void setReturnValue(List<StackFrame> frames, Value returnValue) {
		if(frames.size() == countActive) {
			for(int i = 0; i < frames.size(); i++) {
				if(!sameLocation(frames.get(i).location(), callStack.get(countActive-1-i)))
					return;
			
			}
			callStack.get(countActive-1).setReturnValue(returnValue);
		}
	}


	

	


	
}
