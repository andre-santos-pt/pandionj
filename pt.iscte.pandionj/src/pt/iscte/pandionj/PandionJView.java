package pt.iscte.pandionj;

import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
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
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.ZestStyles;

import pt.iscte.pandionj.model.CallStackModel;
import pt.iscte.pandionj.model.StackFrameModel;

public class PandionJView extends ViewPart { 

	private CallStackModel model;
	private IStackFrame exceptionFrame;
	private String exception;

	//	private IDebugContextListener debugUiListener;
	private IDebugEventSetListener debugEventListener;
	private IJavaBreakpointListener exceptionListener;

	private ScrolledComposite scroll; 
	private Composite area;
	private ExpandBar callStack;
	private Label label;
	private Image image;
	private Image imageRun;

	private double zoom;

	@Override
	public void createPartControl(Composite parent) {
		createWidgets(parent);
		addToolBarItems();
		image = new Image(Display.getDefault(), PandionJView.class.getResourceAsStream("frame.gif")); 
		imageRun = new Image(Display.getDefault(), PandionJView.class.getResourceAsStream("frame_run.gif"));
		zoom = 1.0;

		model = new CallStackModel();
		debugEventListener = (new DebugListener());
		//		debugUiListener = new DebugUIListener();
		exceptionListener = new ExceptionListener();

		DebugPlugin.getDefault().addDebugEventListener(debugEventListener);
		JDIDebugModel.addJavaBreakpointListener(exceptionListener);
		//		DebugUITools.getDebugContextManager().addDebugContextListener(debugUiListener);


	}

	@Override
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().removeDebugEventListener(debugEventListener);
		JDIDebugModel.removeJavaBreakpointListener(exceptionListener);
		//		DebugUITools.getDebugContextManager().removeDebugContextListener(debugUiListener);
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
		scroll.setMinHeight(100);
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
		FontManager.setFont(label, Constants.MESSAGE_FONT_SIZE);
		label.setText(Constants.START_MESSAGE);
		label.addMouseListener(new MouseAdapter() {
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
					terminate();
				}
			}
		}

		private void terminate() {
			//clearView(true);

			Display.getDefault().asyncExec(() -> {
				//				label.setText("Execution terminated");
				for (Control view : callStack.getChildren()) {
					view.setEnabled(false);
				}
			});
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
				if(!thread.isPerformingEvaluation()) { // TODO nao funciona
					try {
						exception = null;
						exceptionFrame = null;
						Display.getDefault().syncExec(() -> { clearView(false); });
						IStackFrame[] frames = thread.getStackFrames(); 
						handleFrames(frames);
					} catch (DebugException e) {
						e.printStackTrace();
					}
					return IJavaBreakpointListener.SUSPEND;
				}
			}
			else if (breakpoint instanceof IJavaExceptionBreakpoint) {
				IJavaExceptionBreakpoint exc = (IJavaExceptionBreakpoint) breakpoint;
				try {
					thread.terminateEvaluation();
					exception = exc.getExceptionTypeName();
					exceptionFrame = thread.getTopStackFrame();
					IStackFrame[] frames = thread.getStackFrames(); 

					handleFrames(frames);
					int line = exceptionFrame.getLineNumber();
					model.getTopFrame().processException();
					//					thread.terminate();

					Display.getDefault().asyncExec(() -> {
						String msg = exception + " on line " + line;
						ExpandItem item = callStack.getItem(callStack.getItemCount()-1);
						item.setText(item.getText() + " " + msg);
						StackView view = (StackView) item.getControl();
						view.setError("Exception: " + msg);
					});

				} catch (DebugException e) {
					e.printStackTrace();
				}
				return IJavaBreakpointListener.SUSPEND;
			}
			return IJavaBreakpointListener.DONT_SUSPEND;
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
		Display.getDefault().asyncExec(() -> {
			label.setText(startMsg ? Constants.START_MESSAGE : "");
			area.setBackground(null);
		});
	}



	private void handleFrames(IStackFrame[] frames) {
		assert frames != null;
		model.handle(frames);
		model.update();
		List<StackFrameModel> stackPath = model.getStackPath();
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				Set<StackFrameModel> expanded = new HashSet<>();
				for(ExpandItem item : callStack.getItems())
					if(item.getExpanded())
						expanded.add(((StackView) item.getControl()).model);

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
					items[i].setExpanded(i == items.length-1 || expanded.contains(model));
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
				model.simulateGC();
			}
		});

		toolBar.add(new Action("REC") {
			@Override
			public void run() {
				System.out.println("REC!");
			}
		});

		toolBar.add(new Action("Zoom in", ImageDescriptor.createFromFile( PandionJView.class, "zoom.gif")) {
			public void run() {
				zoom *= 1.05;
				for (ExpandItem expandItem : callStack.getItems()) {
					((StackView) expandItem.getControl()).setZoom(zoom);
				}
			}
		});

		toolBar.add(new Action("-") {

			public void run() {
				zoom *= 0.95;
				for (ExpandItem expandItem : callStack.getItems()) {
					((StackView) expandItem.getControl()).setZoom(zoom);
				}
			}
		});


		Action hightlightAction = new Action("Highlight", Action.AS_CHECK_BOX) {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
			}
		};
		Image image = new Image(Display.getDefault(), PandionJView.class.getResourceAsStream("highlight.gif"));
		hightlightAction.setImageDescriptor(ImageDescriptor.createFromImage(image));
		toolBar.add(hightlightAction);

		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
		menuManager.add(new Action("highlight color") {
		});
		menuManager.add(new Action("Copy canvas to clipboard") {
			@Override
			public void run() {
				if(callStack.getItemCount() > 0) {
					ExpandItem item = callStack.getItem(callStack.getItemCount()-1);
					Control control = item.getControl();
					Point size = control.getSize();
			        GC gc = new GC(control);
			        Image img = new Image(Display.getDefault(), size.x, size.y);
			        gc.copyArea(img, 0, 0);
			       
					Clipboard clipboard = new Clipboard(Display.getDefault());
					clipboard.setContents(new Object[]{img.getImageData()}, new Transfer[]{ ImageTransfer.getInstance()}); 
//					img.dispose();
					 gc.dispose();
				}

			}
		});
	}

	private class StackView extends Composite {
		GraphViewerZoomable viewer;
		StackFrameModel model;

		public StackView(Composite parent, ExpandItem barItem) {
			super(parent, SWT.BORDER);
			FillLayout fillLayout = new FillLayout();
			fillLayout.marginHeight = 5;
			fillLayout.marginWidth = 5;

			setLayout(fillLayout);
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			viewer = new GraphViewerZoomable(this, SWT.BORDER);
			PandionJLayoutAlgorithm layout = new PandionJLayoutAlgorithm();
			viewer.setLayoutAlgorithm(layout);
			viewer.setContentProvider(new NodeProvider());
			viewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
			viewer.setLabelProvider(new FigureProvider(viewer.getGraphControl()));
			viewer.getGraphControl().addControlListener(new ControlAdapter() {
				public void controlResized(ControlEvent e) {
					adaptBarHeight(barItem);
				}
			});
			viewer.addDoubleClickListener(new IDoubleClickListener() {

				@Override
				public void doubleClick(DoubleClickEvent event) {
					System.out.println("?? " + event.getSelection());
					if(!event.getSelection().isEmpty()) {
						IStructuredSelection selection = (IStructuredSelection) event.getSelection();
						Object obj = selection.getFirstElement();
						GraphItem item = viewer.findGraphItem(obj);
						Object data = item.getData();
					}
				}
			});
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {

				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					System.out.println(event.getSelection());
				}
			});
			setZoom(zoom);
		}

		public void setZoom(double zoom) {
			viewer.setZoom(zoom);
		}

		public void setError(String message) {
			setBackground(Constants.ERROR_COLOR);
			setToolTipText(message);
		}

		void setInput(StackFrameModel frameModel) {
			if(this.model == frameModel) {
//				this.model.update();
			}
			else {
				this.model = frameModel;
				viewer.setInput(frameModel);
				if (frameModel != null)
					frameModel.registerObserver(new Observer() {
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

	private void adaptBarHeight(ExpandItem barItem) {
		Point size = barItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		barItem.setHeight(Math.max(100, size.y + Constants.MARGIN));
	}


}
