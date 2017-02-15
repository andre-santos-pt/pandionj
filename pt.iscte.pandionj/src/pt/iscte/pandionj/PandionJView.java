package pt.iscte.pandionj;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Semaphore;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.widgets.ZestStyles;

import pt.iscte.pandionj.model.CallStackModel;
import pt.iscte.pandionj.model.StackFrameModel;
import pt.iscte.pandionj.parser.data.NullableOptional;

public class PandionJView extends ViewPart { 
	
	private CallStackModel model;
	private IStackFrame exceptionFrame;
	private String exception;
	
	private IDebugContextListener debugUiListener;
	private IDebugEventSetListener debugEventListener;
	private IJavaBreakpointListener exceptionListener;
	
	private ScrolledComposite scroll; 
	private Composite area;
	private ExpandBar callStack;
	private Label label;
	private Image image;
	private Image imageRun;
	

	@Override
	public void createPartControl(Composite parent) {
		createWidgets(parent);
		addToolBarItems();
		image = new Image(Display.getDefault(), PandionJView.class.getResourceAsStream("frame.gif")); 
		imageRun = new Image(Display.getDefault(), PandionJView.class.getResourceAsStream("frame_run.gif"));
		
		model = new CallStackModel();
		debugEventListener = (new DebugListener());
		debugUiListener = new DebugUIListener();
		exceptionListener = new ExceptionListener();

		DebugPlugin.getDefault().addDebugEventListener(debugEventListener);
//		DebugUITools.getDebugContextManager().addDebugContextListener(debugUiListener);
		JDIDebugModel.addJavaBreakpointListener(exceptionListener);
	}

	@Override
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().removeDebugEventListener(debugEventListener);
		DebugUITools.getDebugContextManager().removeDebugContextListener(debugUiListener);
		JDIDebugModel.removeJavaBreakpointListener(exceptionListener);
		image.dispose();
		imageRun.dispose();
	}

	private void createWidgets(Composite parent) {
		parent.setLayout(new FillLayout());
		
		scroll = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL );
		area = new Composite(scroll, SWT.NONE);

		scroll.setContent(area);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.setMinHeight(0);
		scroll.setMinWidth(300);

		GridLayout layout = new GridLayout(1, true);
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.horizontalSpacing = 1;
		layout.verticalSpacing = 1;
		area.setLayout(layout);

		label = new Label(area, SWT.NONE);
		label.setFont(new Font(null, Constants.FONT_FACE, Constants.INDEX_FONT_SIZE, SWT.NONE));
		label.setText(Constants.START_MESSAGE);
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				System.out.println("OPEN FILE"); // TODO
			}
		});

		callStack = new ExpandBar(area, SWT.NONE);
		callStack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}
	
	@Override
	public void setFocus() {
		scroll.setFocus();
	}

	
	
	private class DebugListener implements IDebugEventSetListener {
		public void handleDebugEvents(DebugEvent[] events) {
			if(events.length > 0) {
				DebugEvent e = events[0];
				if(e.getKind() == DebugEvent.SUSPEND && e.getDetail() == DebugEvent.STEP_END && exception == null) {
					IThread thread = (IThread) e.getSource();
					try {
						handleFrames(thread.getStackFrames());
					} catch (DebugException ex) {
						ex.printStackTrace();
					}
				}
				else if(e.getKind() == DebugEvent.TERMINATE) {
					clearView(true);
				}
			}
		}
	}

	

	private class DebugUIListener implements IDebugContextListener {
		public void debugContextChanged(DebugContextEvent event) {
			IStackFrame f = getSelectedFrame(event.getContext());
			if(f != null && (event.getFlags() & DebugContextEvent.ACTIVATED) != 0) {
				openExpandItem(f);
			}
		}

		private void openExpandItem(IStackFrame f) {
			for(ExpandItem e : callStack.getItems())
				e.setExpanded(((StackView) e.getControl()).model.getStackFrame() == f);
		}

		private IStackFrame getSelectedFrame(ISelection context) {
			if (context instanceof IStructuredSelection) {
				Object data = ((IStructuredSelection) context).getFirstElement();
				if (data instanceof IStackFrame)
					return (IStackFrame) data;
			}
			return null;
		}
	}


	private class ExceptionListener implements IJavaBreakpointListener {
		public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
			if(breakpoint instanceof IJavaLineBreakpoint) {
				try {
					exception = null;
					exceptionFrame = null;
					Display.getDefault().syncExec(() -> { clearView(false); });
					IStackFrame[] frames = thread.getStackFrames(); 
					handleFrames(frames);
				} catch (DebugException e) {
					e.printStackTrace();
				}

			}
			else if (breakpoint instanceof IJavaExceptionBreakpoint) {
				IJavaExceptionBreakpoint exc = (IJavaExceptionBreakpoint) breakpoint;
				try {
					exception = exc.getExceptionTypeName();
					exceptionFrame = thread.getTopStackFrame();
					IStackFrame[] frames = thread.getStackFrames(); 
					handleFrames(frames);
					int line = exceptionFrame.getLineNumber();
					Display.getDefault().syncExec(() -> {
						area.setBackground(Constants.ERROR_COLOR);
						label.setText("Error: " + exception + " on line " + line);
					});
					model.getTopFrame().processException();
				} catch (DebugException e) {
					e.printStackTrace();
				}
			}
			return IJavaBreakpointListener.DONT_CARE;
		}

		public int installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type) {
			return IJavaBreakpointListener.DONT_CARE;
		}

		public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint) { }
		public void breakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint) { }
		public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, DebugException exception) { }
		public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint) { }
		public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors) { }
	}



	private void clearView(boolean startMsg) {
		exception = null;
		exceptionFrame = null;
		model = new CallStackModel();
		handleFrames(new IStackFrame[0]);
		Display.getDefault().syncExec(() -> {
			label.setText(startMsg ? Constants.START_MESSAGE : "");
			area.setBackground(null);
		});
	}



	private void handleFrames(IStackFrame[] frames) {
		assert frames != null;
		model.handle(frames);
		model.update();
		List<StackFrameModel> stackPath = model.getStackPath();
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				int diff = stackPath.size() - callStack.getItemCount();
				while(diff > 0) {
					diff--;
					ExpandItem e = new ExpandItem(callStack, SWT.NONE);
					StackView view = new StackView(callStack, e);
					e.setControl(view);
					e.setHeight(100);
					
				}
				while(diff < 0) {
					diff++;
					ExpandItem item = callStack.getItem(callStack.getItemCount()-1);
					item.getControl().dispose();
					item.dispose();
				}

				assert callStack.getItemCount() == stackPath.size();

				ExpandItem[] items = callStack.getItems();
				for(int i = 0; i < items.length; i++) {
					StackFrameModel model = stackPath.get(i);
					StackView view = (StackView) items[i].getControl();
					view.setInput(model);
					items[i].setText(model.toString());
					items[i].setExpanded(i == items.length-1);
					items[i].setImage(i == items.length-1 ? imageRun : image);
				}
			}
		});
	}




	private void addToolBarItems() {
		IToolBarManager toolBar = getViewSite().getActionBars().getToolBarManager();
		toolBar.add(new Action("GC") {
			@Override
			public void run() {
				System.out.println("GC!");
			}
		});

		toolBar.add(new Action("REC") {
			@Override
			public void run() {
				System.out.println("REC!");
			}
		});

		toolBar.add(new Action("+") {
			@Override
			public void run() {
				for (ExpandItem expandItem : callStack.getItems()) {
					((StackView) expandItem.getControl()).increaseZoom();
				}
			}
		});

		toolBar.add(new Action("-") {
			@Override
			public void run() {
				for (ExpandItem expandItem : callStack.getItems()) {
					((StackView) expandItem.getControl()).decreaseZoom();
				}
			}
		});
	}

	private class StackView extends Composite {
		GraphViewerZoomable viewer;
		StackFrameModel model;
		ExpandItem barItem;
		public StackView(Composite parent, ExpandItem barItem) {
			super(parent, SWT.BORDER);
			this.barItem = barItem;
			setLayout(new FillLayout());
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			viewer = new GraphViewerZoomable(this, SWT.BORDER);
			viewer.setLayoutAlgorithm(new PandionJLayoutAlgorithm()); // SpringLayoutAlgorithm(ZestStyles.NODES_NO_LAYOUT_RESIZE));
			viewer.setContentProvider(new NodeProvider());
			viewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
			viewer.setLabelProvider(new FigureProvider());
			viewer.getGraphControl().addControlListener(new ControlAdapter() {
				public void controlResized(ControlEvent e) {
					adaptBarHeight(barItem);
				}
			});
		}

		public void decreaseZoom() {
			viewer.decreaseZoom();
		}

		public void increaseZoom() {
			viewer.increaseZoom();
		}

		void setInput(StackFrameModel frameModel) {
			if(this.model == frameModel) {
				this.model.update();
			}
			else {
				this.model = frameModel;
				viewer.setInput(frameModel);
				if (frameModel != null)
					frameModel.addObserver(new Observer() {
						public void update(Observable o, Object e) {
							Display.getDefault().asyncExec(() -> {
								viewer.refresh();
								viewer.applyLayout();
							});
						}
					});
			}
//			adaptBarHeight(barItem);
		}
	}

	
	
	private void adaptBarHeight(ExpandItem barItem) {
		Point size = barItem.getControl().computeSize(area.getBounds().width, SWT.DEFAULT);
		barItem.setHeight(Math.max(100, size.y + Constants.MARGIN));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private static NullableOptional<String> valueOfExpression(IStackFrame stackFrame, String expression) {
		// TODO fix code. This is a work-around making asynchronous
		// WatchExpressionDelegate synced because WatchExpression wouldn't work
		IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();
		IWatchExpressionDelegate delegate = expressionManager
				.newWatchExpressionDelegate(stackFrame.getModelIdentifier());
		class Wrapper<T> {
			T value = null;
		}
		;
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
		} catch (InterruptedException e) {
		}

		IValue value = res.value;
		NullableOptional<String> result = null;
		try {
			if (value == null) {
				System.out.println("EVAL <" + expression + ">" + " yields empty");
				result = NullableOptional.ofEmpty();
			} else if ("null".equals(value.getValueString())) {
				System.out.println("EVAL <" + expression + ">" + " yields null");
				result = NullableOptional.ofNull();
			} else {
				System.out.println("EVAL <" + expression + ">" + " yields " + value.getValueString());
				result = NullableOptional.ofNonNull(value.getValueString());
			}
		} catch (DebugException e) {
		} finally {
			if (result == null)
				result = NullableOptional.ofEmpty();
		}
		return result;
	}

	private static boolean isException(String referenceTypeName) {
		try {
			Class<?> referenceClass = Class.forName(referenceTypeName);
			return Exception.class.isAssignableFrom(referenceClass);
		} catch (ClassNotFoundException e) {

			return false;
		}
	}

}
