package pt.iscte.pandionj;

import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Semaphore;

import org.eclipse.core.resources.IMarker;
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
import org.eclipse.draw2d.ColorConstants;
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
import org.eclipse.swt.events.ExpandAdapter;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.internal.ZoomManager;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.ZestStyles;

import pt.iscte.pandionj.model.CallStackModel;
import pt.iscte.pandionj.model.ModelElement;
import pt.iscte.pandionj.model.StackFrameModel;
import pt.iscte.pandionj.parser.data.NullableOptional;

public class PandionJView extends ViewPart { 
	private CallStackModel model;
	private Composite area;

	private ExpandBar callStack;
	private Label label;

	private String exception;
	private IStackFrame exceptionFrame;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		ScrolledComposite scroll = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL );
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
		label.setText("This view will be populated once the execution of the Java debugger hits a breakpoint.");
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				System.out.println("OPEN FILE"); // TODO
			}
		});
		model = new CallStackModel();

		callStack = new ExpandBar(area, SWT.NONE);
		callStack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		System.out.println(callStack.getBackground());
		DebugUITools.getDebugContextManager().addDebugContextListener(new DebugListener());
		JDIDebugModel.addJavaBreakpointListener(new ExceptionListener());
		addToolBarItems();
	}


	private class DebugListener implements IDebugContextListener {

		@Override
		public void debugContextChanged(DebugContextEvent event) {
			IStackFrame f = getSelectedFrame(event.getContext());
			if(exception != null) {
				area.setBackground(Constants.ERROR_COLOR);
				try {
					label.setText("Error: " + exception + " on line " + exceptionFrame.getLineNumber());
				} catch (DebugException e) {
					e.printStackTrace();
				}
				model.getTopFrame().processException();
			}
			else {
				if ((event.getFlags() & DebugContextEvent.STATE) != 0) {
					try {
						IStackFrame[] frames = f == null ? new IStackFrame[0] : f.getThread().getStackFrames();
						handleFrames(frames);
					}
					catch (DebugException e) {
						e.printStackTrace();
					}
				}
				else if(f != null && (event.getFlags() & DebugContextEvent.ACTIVATED) != 0) {
					select(f);
				}
			}
		}

		private void select(IStackFrame f) {
			for(ExpandItem e : callStack.getItems())
				e.setExpanded(((StackView) e.getControl()).model.getStackFrame() == f);
		}

		private IStackFrame getSelectedFrame(ISelection context) {
			if (context instanceof IStructuredSelection) {
				Object data = ((IStructuredSelection) context).getFirstElement();
				System.out.println(data);
				if (data instanceof IStackFrame)
					return (IStackFrame) data;
			}
			return null;
		}
	}

	final Image image = new Image(Display.getDefault(), PandionJView.class.getResourceAsStream("frame.gif")); 
	final Image imageRun = new Image(Display.getDefault(), PandionJView.class.getResourceAsStream("frame_run.gif")); 

	private void handleFrames(IStackFrame[] frames) {
		assert frames != null;
		model.handle2(frames);
		model.update();
		List<StackFrameModel> stackPath = model.getStackPath();
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				int diff = stackPath.size() - callStack.getItemCount();
				while(diff > 0) {
					diff--;
					ExpandItem e = new ExpandItem(callStack, SWT.NONE);
					StackView view = new StackView(callStack);
					e.setControl(view);
					e.setHeight(400);
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
					((StackView) items[i].getControl()).setInput(model);
					items[i].setText(model.toString());
					items[i].setExpanded(i == items.length-1);
					items[i].setImage(i == items.length-1 ? imageRun : image);
				}
			}
		});
	}


	private class ExceptionListener implements IJavaBreakpointListener {
		public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
			if(breakpoint instanceof IJavaLineBreakpoint) {
				try {
					exception = null;
					exceptionFrame = null;
					Display.getDefault().syncExec(() -> {
						label.setText("");
						area.setBackground(null);
					});
					IStackFrame[] frames = thread.getStackFrames(); 
					handleFrames(frames);
				} catch (DebugException e) {
					e.printStackTrace();
				}

			}
			else if (breakpoint instanceof IJavaExceptionBreakpoint) {
				IJavaExceptionBreakpoint exc = (IJavaExceptionBreakpoint) breakpoint;
				//				IMarker marker = exc.getMarker();//thread.getTopStackFrame().getLineNumber()
				try {
					exception = exc.getExceptionTypeName();
					exceptionFrame = thread.getTopStackFrame();
					//					System.out.println("EXC:" + exc.getExceptionTypeName() + " " + thread.getTopStackFrame().getLineNumber());
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
		MyGraphViewer viewer;
		StackFrameModel model;

		public StackView(Composite parent) {
			super(parent, SWT.BORDER);
			setLayout(new FillLayout());
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			viewer = new MyGraphViewer(this, SWT.BORDER);
			viewer.setLayoutAlgorithm(new PandionJLayoutAlgorithm()); // SpringLayoutAlgorithm(ZestStyles.NODES_NO_LAYOUT_RESIZE));
			viewer.setContentProvider(new NodeProvider());
			viewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
			viewer.setLabelProvider(new FigureProvider());

			Graph graph = viewer.getGraphControl();

			Menu menu = new Menu(graph);
			MenuItem recItem = new MenuItem(menu, SWT.TOGGLE);
			recItem.setText("Recording");
			recItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					System.out.println("Rec...");
				}
			});
			graph.setMenu(menu);
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
							Display.getDefault().syncExec(() -> {
								viewer.refresh();
								viewer.applyLayout();
							});
						}
					});
			}
		}
	}

	private static class MyGraphViewer extends GraphViewer {

		MyGraphViewer(Composite composite, int style) {
			super(composite, style);
			getZoomManager().setZoom(1);
		}

		public void decreaseZoom() {
			ZoomManager mng = getZoomManager();
			mng.setZoom(mng.getZoom()*.95);
		}

		void increaseZoom() {
			ZoomManager mng = getZoomManager();
			mng.setZoom(mng.getZoom()*1.05);
		}
	}

	@Override
	public void setFocus() {
		// viewer.getControl().setFocus();
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
