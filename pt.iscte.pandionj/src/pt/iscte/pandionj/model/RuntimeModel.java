package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;

import pt.iscte.pandionj.PandionJView;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IObjectModel.InvocationResult;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IRuntimeModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;


public class RuntimeModel
extends DisplayUpdateObservable<IRuntimeModel.Event<IStackFrameModel>>
implements IRuntimeModel {

	private ILaunch launch;
	private List<StackFrameModel> callStack;
	private Map<Long, EntityModel<?>> objects;
	private Map<Long, EntityModel<?>> looseObjects;
	private StaticRefsContainer staticRefs;

	private int countActive;
	private boolean terminated;

	private int step;

	public RuntimeModel() {
		callStack = new ArrayList<>();
		objects = new HashMap<>();
		looseObjects = new HashMap<>();
		staticRefs = new StaticRefsContainer(this);

		countActive = 0;
		terminated = false;
		step = 0;
	}

	public IJavaDebugTarget getDebugTarget() {
		return (IJavaDebugTarget) launch.getDebugTarget();
	}

	public void update(IJavaThread thread) throws DebugException {
		if(launch != thread.getLaunch()) {	
			launch = thread.getLaunch();
			callStack.clear();
			objects.clear();
			looseObjects.clear();
			step = 0;
			terminated = false;
			countActive = 0;
			setChanged();
			notifyObservers(new Event<IStackFrameModel>(Event.Type.NEW_STACK, null));
		}

		//		for(EntityModel<?> o : objects.values().toArray(new EntityModel[objects.size()])) {
		//			if(o instanceof ArrayModel && o.update(step))
		//				setChanged();
		//			else if(o instanceof ObjectModel) {
		//				((ObjectModel) o).traverseSiblings(new SiblingVisitor() {
		//					public void visit(IEntityModel object, ObjectModel parent, int index, int depth, String field) {
		//						try {
		//							if(object != null && ((EntityModel<?>) object).update(step))
		//								setChanged();
		//						}
		//						catch(DebugException e) {
		//							//							throw e; // TODO propagate exception
		//						}
		//					}
		//				});
		//			}
		//		}

		PandionJView.getInstance().executeInternal(() -> {
			handle(thread.getStackFrames());
		});

		updateActiveStack();

		step++;
		setChanged();
		notifyObservers(new Event<IStackFrameModel>(Event.Type.STEP, getTopFrame()));

	}

	public void evaluate(String expression, InvocationResult listener) {
		IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();
		StackFrameModel stackFrame = getTopFrame();
		IWatchExpressionDelegate delegate = expressionManager.newWatchExpressionDelegate(stackFrame.getStackFrame().getModelIdentifier());	
		delegate.evaluateExpression(expression, stackFrame.getStackFrame(), new IWatchExpressionListener() {
			public void watchEvaluationFinished(IWatchExpressionResult result) {
				listener.valueReturn(result.getValue());
//				setChanged();
//				notifyObservers(new Event<IStackFrameModel>(Event.Type.EVALUATION, getTopFrame()));
//				try {
//					evaluationNotify();
//				} catch (DebugException e) {
//					e.printStackTrace();
//				}
			}
		});
	}

	public void evaluationNotify() throws DebugException {
		updateActiveStack();
		setChanged();
		notifyObservers(new Event<IStackFrameModel>(Event.Type.EVALUATION, getTopFrame()));
	}

	public void updateActiveStack() throws DebugException {
		for(int i = 0; i < countActive; i++)
			callStack.get(i).update();

		List<Long> toRemove = new ArrayList<>();

		for(EntityModel<?> m : new ArrayList<EntityModel<?>>(objects.values())) // to avoid concurrent modification
			if(m.isAllocated())
				m.update(0);
			else
				toRemove.add(m.getContent().getUniqueId());

		for(Long id : toRemove)
			objects.remove(id);

	}

	private void handle(IStackFrame[] stackFrames) throws DebugException {
		assert stackFrames != null;

		IStackFrame[] frames = reverse(stackFrames);
		if(isSubStack(frames)) {
			for(int i = frames.length; i < callStack.size(); i++)
				callStack.get(i).setObsolete();

			countActive = frames.length;
		}
		else if(isStackIncrement(frames)) {
			for(int i = callStack.size(); i < frames.length; i++) {
				StackFrameModel newFrame = new StackFrameModel(this, (IJavaStackFrame) frames[i], staticRefs);
				callStack.add(newFrame);
				countActive++;
				setChanged();
				notifyObservers(new Event<IStackFrameModel>(Event.Type.NEW_FRAME, newFrame));	
			}
		}
		else {
			int offset = 0;
			while(offset < callStack.size() && offset < frames.length && 
					callStack.get(offset).getStackFrame() == frames[offset]) {
				offset++;
			}

			for(int i = callStack.size() - offset; i > 0; i--) {
				StackFrameModel s = callStack.remove(offset);
				setChanged();
				notifyObservers(new Event<IStackFrameModel>(Event.Type.REMOVE_FRAME, s));
			}

			countActive = offset;

			for(int i = offset; i < frames.length; i++) {
				StackFrameModel newFrame = new StackFrameModel(this, (IJavaStackFrame) frames[i], staticRefs);
				callStack.add(newFrame);
				countActive++;
				setChanged();
				notifyObservers(new Event<IStackFrameModel>(Event.Type.NEW_FRAME, newFrame));	
			}
		}
	}

	public boolean isPartiallyCommon(IStackFrame[] stackFrames) {
		IStackFrame[] reverse = reverse(stackFrames);
		return isSubStack(reverse) || isStackIncrement(reverse);
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

	public StackFrameModel getFirstVisibleFrame() {
		assert !isEmpty();
		for(int i = 0; i < countActive; i++) {
			StackFrameModel f = callStack.get(i);
			if(f.getLineNumber() != -1)
				return f;
		}
		return null;
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


	public StackFrameModel getFrame(IStackFrame exceptionFrame) {
		for(StackFrameModel s : callStack)
			if(s.getStackFrame() == exceptionFrame)
				return s;

		assert false;
		return null;
	}

	public void setTerminated() {
		if(launch != null)
			try {
				launch.terminate();
			}
		catch (DebugException e) {
			//				e.printStackTrace();
		}

		terminated = true;
		for(StackFrameModel frame : callStack)
			frame.setObsolete();

		setChanged();
		notifyObservers(new Event<IStackFrameModel>(Event.Type.TERMINATION, null));
	}

	public boolean isTerminated() {
		return terminated;
	}

	public int getRunningStep() {
		return step;
	}


	public IEntityModel getObject(IJavaObject obj, boolean loose, IReferenceModel model) {
		assert !obj.isNull();

		return PandionJView.getInstance().executeInternal(() -> {
			EntityModel<?> e = objects.get(obj.getUniqueId());
			if(e == null) {
				if(obj.getJavaType() instanceof IJavaArrayType) {
					IJavaArray array = (IJavaArray) obj;
					boolean refType = array.getSignature().startsWith("[L") || array.getSignature().startsWith("[[");
					if(refType)
						e = new ArrayReferenceModel((IJavaArray) obj, this, model);
					else
						e = new ArrayPrimitiveModel((IJavaArray) obj, this, model);
				}
				else {
					IType type = null;
					try {
						IJavaProject javaProject = getTopFrame().getJavaProject();
						if(javaProject != null)
							type = javaProject.findType(obj.getJavaType().getName());
					} catch (JavaModelException e1) {
						e1.printStackTrace();
					}
					e = new ObjectModel(obj, type, this);
				}

				if(loose) {
					looseObjects.put(obj.getUniqueId(), e);
					//					setChanged();
					//					notifyObservers(new Event<IEntityModel>(Event.Type.NEW_OBJECT, e));
				}
				else {
					objects.put(obj.getUniqueId(), e);
				}
			}
			return e;
		}, null);
	}

	public Collection<IEntityModel> getLooseObjects() {
		return Collections.unmodifiableCollection(looseObjects.values());
	}


	public void setReturnOnFrame(StackFrameModel current, IJavaValue returnValue) {
		assert callStack.contains(current);
		if(!returnValue.toString().equals("(void)")) {
			int i = callStack.indexOf(current);
			if(i + 1 < callStack.size())
				callStack.get(i+1).setReturnValue(returnValue);
		}
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
		setChanged();
		notifyObservers(new Event<IStackFrameModel>(Event.Type.GARBAGE_COLLECTION, null));
	}

	public IStackFrameModel getStaticVars() {
		return staticRefs;
	}

	public List<IReferenceModel> findReferences(IEntityModel e) {
		List<IReferenceModel> list = new ArrayList<>(staticRefs.getReferencesTo(e));
		for(int i = 0; i < countActive; i++)
			list.addAll(callStack.get(i).getReferencesTo(e));

		return list;
	}

	public List<String> findReferencePaths(IEntityModel target) {
		StackFrameModel firstVisibleFrame = getFirstVisibleFrame();
		if(firstVisibleFrame == null)
			return Collections.emptyList();
		
		Collection<IReferenceModel> refs = firstVisibleFrame.getReferenceVariables();
		Collection<IEntityModel> visited = new ArrayList<IEntityModel>();
		Stack<String> stack = new Stack<String>();
		List<String> paths = new ArrayList<String>();
		
		for(IReferenceModel r : refs)
			findReferencePathsRec(target, r, visited, stack, paths);
		
		return paths;
	}


	private static void findReferencePathsRec(IEntityModel target, IReferenceModel r, Collection<IEntityModel> visited, Stack<String> stack, List<String> paths) {
		IEntityModel e = r.getModelTarget();
		if(e.isNull() || visited.contains(e))
			return;
		
//		String s = r.getName();
//		int index = r.getIndex();
//		if(index != -1)
//			s += "[" + index + "]";

		visited.add(e);
		stack.add(r.getName());
		
		if(e == target) {
			String path = String.join(".", stack);
			path = path.replaceAll("\\.\\[", "[");
			paths.add(path);
			return;
		}
		
		if(e instanceof ObjectModel) {
			ObjectModel o = (ObjectModel) e;
			for(IReferenceModel or : o.getReferenceFields())
				findReferencePathsRec(target, or, visited, stack, paths);
		}
		else if(e instanceof ArrayReferenceModel) {
			ArrayReferenceModel array = (ArrayReferenceModel) e;
			for (IReferenceModel ar : array.getModelElements())
				findReferencePathsRec(target, ar, visited, stack, paths);
		}
		stack.pop();
	}

	
	public Collection<IEntityModel> findReachableObjects() {
		StackFrameModel firstVisibleFrame = getFirstVisibleFrame();
		if(firstVisibleFrame == null)
			return Collections.emptyList();
		
		Collection<IReferenceModel> refs = firstVisibleFrame.getReferenceVariables();
		Collection<IEntityModel> visited = new ArrayList<IEntityModel>();
		
		for(IReferenceModel r : refs)
			findReachableObjectsRec(r, visited);
		
		return visited;
	}

	private static void findReachableObjectsRec(IReferenceModel r, Collection<IEntityModel> visited) {
		IEntityModel e = r.getModelTarget();
		if(e.isNull() || visited.contains(e))
			return;
		
		visited.add(e);
		
		if(e instanceof ObjectModel) {
			ObjectModel o = (ObjectModel) e;
			for(IReferenceModel or : o.getReferenceFields())
				findReachableObjectsRec(or, visited);
		}
		else if(e instanceof ArrayReferenceModel) {
			ArrayReferenceModel array = (ArrayReferenceModel) e;
			for (IReferenceModel ar : array.getModelElements())
				findReachableObjectsRec(ar, visited);
		}
	}
}
