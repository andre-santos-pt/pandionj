package pt.iscte.pandionj;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Semaphore;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.ZestStyles;

import pt.iscte.pandionj.model.CallStackModel;
import pt.iscte.pandionj.model.StackFrameModel;
import pt.iscte.pandionj.parser.data.NullableOptional;


public class PandionJView extends ViewPart implements IZoomableWorkbenchPart {

	private StackView view;
	private CallStackModel model;
	
	@Override
	public AbstractZoomableViewer getZoomableViewer() {
		return view.viewer;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		view = new StackView(parent, 0);
		model = new CallStackModel();
		
		IDebugContextListener listener = new IDebugContextListener() {
			@Override
			public void debugContextChanged(DebugContextEvent event) {
				ISelection context = event.getContext();
				
				if (context instanceof StructuredSelection) {
					Object data = ((StructuredSelection) context).getFirstElement();
					if (data instanceof IStackFrame) {
						IStackFrame stackFrame = (IStackFrame) data;
						try {
							IStackFrame[] frames = stackFrame.getThread().getStackFrames();
							if(frames.length == 0)
								return;
							
							StackFrameModel top = model.getSize() > 0 ? model.getTopFrame() : null;
							model.handle(frames);
							if(model.getTopFrame() != top)
								view.setInput(model.getTopFrame());
							else
								model.update();
							
						} catch (DebugException e) {
							e.printStackTrace();
						}
					} 
				}

			}
		};
		DebugUITools.getDebugContextManager().addDebugContextListener(listener);
		ZoomContributionViewItem toolbarZoomContributionViewItem = new ZoomContributionViewItem(this);
		IActionBars bars = getViewSite().getActionBars();
		bars.getMenuManager().add(toolbarZoomContributionViewItem);

	}

	
	
	private class StackView extends Composite {
		private GraphViewer viewer;
		
		public StackView(Composite parent, int index) {
			super(parent, SWT.BORDER);
			setLayout(new FillLayout());
			viewer = new GraphViewer(this, SWT.BORDER);
//			viewer.setLayoutAlgorithm(new GridLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING));
			viewer.setLayoutAlgorithm(new PandionJLayoutAlgorithm());
			viewer.setContentProvider(new NodeProvider());
			viewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
			viewer.setLabelProvider(new FigureProvider());
		}
		
		void setInput(StackFrameModel model) {
			viewer.setInput(model);
			model.addObserver(new Observer() {
				public void update(Observable o, Object e) {
					System.out.println(e);
					viewer.refresh();
					viewer.applyLayout();
				}
			});
		}
	}
	
	
	@Override
	public void setFocus() {
//		viewer.getControl().setFocus();
	}

	private static NullableOptional<String> valueOfExpression(IStackFrame stackFrame, String expression) {
		//TODO fix code. This is a work-around making asynchronous WatchExpressionDelegate synced because WatchExpression wouldn't work
		IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();
		IWatchExpressionDelegate delegate = expressionManager.newWatchExpressionDelegate(stackFrame.getModelIdentifier());
		class Wrapper<T> {
			T value = null;
		};
		Wrapper<IValue> res = new Wrapper<>();
		Semaphore sem = new Semaphore(0);

		IWatchExpressionListener valueListener = result -> {
			try {
				res.value = result.getValue();
			} finally {
				sem.release();
			}
		};
		delegate.evaluateExpression(expression, stackFrame, valueListener);

		try {
			sem.acquire();
		} catch (InterruptedException e) { }

		IValue value = res.value;
		NullableOptional<String> result = null;
		try {
			if(value == null){
				System.out.println("EVAL <" + expression + ">" + " yields empty");
				result = NullableOptional.ofEmpty();
			}
			else if("null".equals(value.getValueString())){
				System.out.println("EVAL <" + expression + ">" + " yields null");
				result = NullableOptional.ofNull();
			}else {
				System.out.println("EVAL <" + expression + ">" + " yields " + value.getValueString());
				result = NullableOptional.ofNonNull(value.getValueString());
			}
		} catch (DebugException e) {
		} finally {
			if(result == null)
				result = NullableOptional.ofEmpty();
		}
		return result;
	}

	//	public static void redrawStack(IStackFrame[] frames) throws DebugException {
	//		frames = reverseOf(frames);
	//		String exceptionClassName = null;
	//		for(IStackFrame frame : frames)
	//			for(IVariable var : frame.getVariables())
	//				if(isException(var.getValue().getReferenceTypeName()))
	//					exceptionClassName = var.getValue().getReferenceTypeName();
	//		instance.panel.setFrameSize(frames.length);
	//		for(int i = 0 ; i < frames.length; i++){
	//			IStackFrame frame = frames[i];
	//			File srcFile = (File) frame.getLaunch().getSourceLocator().getSourceElement(frame);
	//			ParserResult parserResult = null;
	//			if(srcFile == null || !srcFile.exists())
	//				break;
	//			parserResult = ParserAPI.parseFile(srcFile.getRawLocation().toString());
	//			instance.panel.setFrameLine(i, frame, frame.getName(), parserResult, frame.getLineNumber());
	//
	//			if(exceptionClassName == null)
	//				for(IVariable var : frame.getVariables()){
	//					if(var.getName().equals("this"))
	//						for (IVariable subVar : var.getValue().getVariables())
	//							updateVariableValue(frame, subVar);
	//					else
	//						updateVariableValue(frame, var);
	//				}
	//		}
	//		instance.panel.draw(exceptionClassName);
	//		//RealView.redraw(stackFrame.getLineNumber(), lastExceptionClassName);
	//	}

	private static IStackFrame[] reverseOf(IStackFrame[] array){
		IStackFrame[] reverse = new IStackFrame[array.length];
		for (int i = 0; i < reverse.length; i++) {
			reverse[i] = array[array.length - 1 - i];
		}
		return reverse;
	}

	private static boolean isException(String referenceTypeName) {
		try {
			Class<?> referenceClass = Class.forName(referenceTypeName);
			return Exception.class.isAssignableFrom(referenceClass);
		} catch (ClassNotFoundException e) {

			return false;
		}
	}

	//	private static void updateVariableValue(IStackFrame stackFrame, IVariable var, boolean addRef, String parentName) throws DebugException{
	//		String typeName = var.getReferenceTypeName();
	//		String valueStr = var.getValue().toString();
	//		if("null".equals(valueStr)){
	//			if(addRef)
	//				instance.panel.updateNullReference(stackFrame, var.getName());
	//		} else if(isPrimitiveTypeName(typeName) || typeName.equals("java.lang.String")){
	//			if(addRef)
	//				instance.panel.updatePrimitiveOrString(stackFrame, var.getName(), valueStr);
	//		} else if(isArrayTypeName(typeName)){
	//			String strippedTypeName = removeArrayBrackets(var.getReferenceTypeName());
	//			int objId = getObjectId(valueStr);
	//			if(addRef)
	//				instance.panel.updateReference(stackFrame, var.getName(), objId);
	//			if(isPrimitiveTypeName(strippedTypeName) || strippedTypeName.equals("java.lang.String")){
	//				IVariable[] subVars = var.getValue().getVariables();
	//				String[] strValues = new String[subVars.length];
	//				for (int i = 0; i < subVars.length; i++) 
	//					strValues[i] = subVars[i].getValue().getValueString();
	//				instance.panel.updatePrimitiveOrStringArray(stackFrame, objId, strValues);
	//			}
	//			else {
	//				IVariable[] subVars = var.getValue().getVariables();
	//				int[] refIds = new int[subVars.length];
	//				for (int i = 0; i < subVars.length; i++){
	//					String subValueStr = subVars[i].getValue().getValueString();
	//					if(subValueStr.equals("null"))
	//						refIds[i] = PandionJArea.NULL_REFERENCE_ID;
	//					else if(isArrayTypeName(subVars[i].getValue().getReferenceTypeName())){
	//						int id = getObjectId(subValueStr);
	//						String arrayTypeName = removeArrayBrackets(subVars[i].getValue().getReferenceTypeName());
	//						refIds[i] = id;
	//						if(isPrimitiveTypeName(arrayTypeName) || arrayTypeName.equals("java.lang.String")){
	//							IVariable[] arrayPrimitiveVars = subVars[i].getValue().getVariables();
	//							String[] strValues = new String[arrayPrimitiveVars.length];
	//							for (int j = 0; j < arrayPrimitiveVars.length; j++) 
	//								strValues[j] = arrayPrimitiveVars[j].getValue().getValueString();
	//							instance.panel.updatePrimitiveOrStringArray(stackFrame, refIds[i], strValues);
	//						} else {
	//							IVariable[] arrayVars = subVars[i].getValue().getVariables();
	//							int[] subIds = new int[arrayVars.length];
	//							for (int j = 0; j < arrayVars.length; j++)
	//								if(arrayVars[j].getValue().getValueString().equals("null"))
	//									subIds[j] = PandionJArea.NULL_REFERENCE_ID;
	//								else if(arrayVars[j].getValue().getReferenceTypeName().equals("java.lang.String")){
	//									String value = arrayVars[j].getValue().toString();
	//									subIds[j] = instance.panel.generateNewObjectId();
	//									instance.panel.updateObject(stackFrame, subIds[j], value);
	//								} else
	//									subIds[j] = getObjectId(arrayVars[j].getValue().toString());
	//							instance.panel.updateReferenceArray(stackFrame, id, subIds);
	//							for(IVariable arrayVar : arrayVars)
	//								if(!arrayVar.getValue().getReferenceTypeName().equals("java.lang.String"))
	//									updateVariableValue(stackFrame, arrayVar, false, "((Object[])" + (parentName == null ? "" : parentName) + var.getName() + subVars[i].getName() + ")");
	//						}
	//					} else if(subVars[i].getValue().getReferenceTypeName().equals("java.lang.String")){
	//						String value = subVars[i].getValue().toString();
	//						refIds[i] = instance.panel.generateNewObjectId();
	//						instance.panel.updateObject(stackFrame, refIds[i], value);
	//					} else {
	//						int id = getObjectId(subValueStr);
	//						refIds[i] = id;
	//						NullableOptional<String> valueOp = valueOfExpression(stackFrame, var.getName() + subVars[i].getName() + ".toString()");
	//						valueOp.ifNonNullPresent(str -> instance.panel.updateObject(stackFrame, id, str));
	//					}
	//				}
	//				instance.panel.updateReferenceArray(stackFrame, objId, refIds);
	//			}
	//		} else {
	//			int objId = getObjectId(var.getValue().toString());
	//			if(addRef)
	//				instance.panel.updateReference(stackFrame, var.getName(), objId);
	//			String varName = (parentName == null? "" : parentName) + var.getName();
	//			final String objToStr = valueOfExpression(stackFrame, varName + ".toString()").orElse("No toString() availlable.");
	//			instance.panel.updateObject(stackFrame, objId, objToStr);
	//		}
	//	}

	//	public static void updateVariableValue(IStackFrame stackFrame, IVariable var) throws DebugException{
	//		updateVariableValue(stackFrame, var, true, null);
	//	}

	private static int getObjectId(String str){
		if(str == null || !str.contains("(id="))
			throw new IllegalArgumentException(str);
		return Integer.parseInt(str.substring(str.indexOf("=")+1, str.indexOf(")")));
	}

	private static boolean isArrayTypeName(String typeName){
		return typeName.indexOf('[') >= 0;
	}

	private static String removeArrayBrackets(String typeName){
		int bracketIndex = typeName.lastIndexOf('[');
		return bracketIndex >= 0 ? typeName.substring(0, bracketIndex) : typeName;
	}

	private static boolean isPrimitiveTypeName(String typeName){
		switch(typeName){
		case "int":
		case "boolean":
		case "double":
		case "float":
		case "char":
		case "byte":
			return true;
		default:
			return false;
		}
	}


}
