package pt.iscte.pandionj;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.osgi.framework.Bundle;

import pt.iscte.pandionj.model.CallStackModel2;
import pt.iscte.pandionj.model.StackFrameModel;

public class PandionJView extends ViewPart { 

	private CallStackModel2 model;
	private IStackFrame exceptionFrame;
	private String exception;

	private IDebugEventSetListener debugEventListener;
	private IJavaBreakpointListener exceptionListener;
	//	private IDebugContextListener debugUiListener;

	private ScrolledComposite scroll; 
	private Composite area;
	private StackView stackView;

	private Label labelInit;

	private StackLayout stackLayout;

	private Map<String, Image> images;
	private IToolBarManager toolBar;
	
	
	public PandionJView() {
		images = new HashMap<>();
	}
	
	@Override
	public void createPartControl(Composite parent) {
		createWidgets(parent);
//		addToolBarItems();

		model = new CallStackModel2();
		debugEventListener = new DebugListener();
		exceptionListener = new ExceptionListener();
		//		debugUiListener = new DebugUIListener();

		DebugPlugin.getDefault().addDebugEventListener(debugEventListener);
		JDIDebugModel.addJavaBreakpointListener(exceptionListener);
		//		DebugUITools.getDebugContextManager().addDebugContextListener(debugUiListener);
		toolBar = getViewSite().getActionBars().getToolBarManager();
		
		addToolbarAction("Run garbage collector", false, "trash.gif", "Simulates the behavior of Java's garbage collector, removing all the unferenced objects.", () -> model.simulateGC());
		addToolbarAction("Zoom in", false, "zoom.gif", null, () -> stackView.zoomIn());
		addToolbarAction("Zoom out", false, "zoom.gif", null, () -> stackView.zoomOut());
		addToolbarAction("Highlight", true, "highlight.gif", "Activates the highlight mode, which ...", () -> {});
		addToolbarAction("Clipboard", false, "clipboard.gif", "Copies the visible area of the top frame as image to the clipboard.", () -> stackView.copyToClipBoard());
	}

	@Override
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().removeDebugEventListener(debugEventListener);
		JDIDebugModel.removeJavaBreakpointListener(exceptionListener);
		//		DebugUITools.getDebugContextManager().removeDebugContextListener(debugUiListener);
		
		for(Image img : images.values())
			img.dispose();
		
		FontManager.dispose();
	}

	
	private Image image(String name) {
		Image img = images.get(name);
		if(img == null) {
			Bundle bundle = Platform.getBundle(Constants.PLUGIN_ID);
			URL imagePath = FileLocator.find(bundle, new Path("images/" + name), null);
			ImageDescriptor imageDesc = ImageDescriptor.createFromURL(imagePath);
			img = imageDesc.createImage();
			images.put(name, img);
		}
		return img;
	}
	
	
	private void createWidgets(Composite parent) {
		stackLayout = new StackLayout();
		parent.setLayout(stackLayout);
		parent.setBackground(new Color(null, 255,255,255));

		scroll = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		area = new Composite(scroll, SWT.NONE);
		area.setBackground(Constants.WHITE_COLOR);

		scroll.setContent(area);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.setMinHeight(400);
		scroll.setMinWidth(600);

		GridLayout layout = new GridLayout(1, true);
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.horizontalSpacing = 1;
		layout.verticalSpacing = 1;
		area.setLayout(layout);

		Composite labelComposite = new Composite(parent, SWT.NONE);
		labelComposite.setLayout(new GridLayout());
		labelInit = new Label(labelComposite, SWT.NONE);
		FontManager.setFont(labelInit, Constants.MESSAGE_FONT_SIZE);
		labelInit.setText(Constants.START_MESSAGE);
		labelInit.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		stackLayout.topControl = labelComposite;

		stackView = new StackView(area);
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
						System.out.println(Arrays.toString(thread.getStackFrames()));
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

			//			Display.getDefault().asyncExec(() -> {
			//				for (Control view : callStack.getChildren()) {
			//					view.setEnabled(false);
			//				}
			//			});
		}
	}




	//	private class DebugUIListener implements IDebugContextListener {
	//		public void debugContextChanged(DebugContextEvent event) {
	//			IStackFrame f = getSelectedFrame(event.getContext());
	//			if(f != null && (event.getFlags() & DebugContextEvent.ACTIVATED) != 0) {
	//				openExpandItem(f);
	//			}
	//		}
	//
	//		private void openExpandItem(IStackFrame f) {
	//			for(ExpandItem e : callStack.getItems())
	//				e.setExpanded(((StackView) e.getControl()).model.getStackFrame() == f);
	//		}
	//
	//		private IStackFrame getSelectedFrame(ISelection context) {
	//			if (context instanceof IStructuredSelection) {
	//				Object data = ((IStructuredSelection) context).getFirstElement();
	//				if (data instanceof IStackFrame)
	//					return (IStackFrame) data;
	//			}
	//			return null;
	//		}
	//	}


	private class ExceptionListener implements IJavaBreakpointListener {
		public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
			if(breakpoint instanceof IJavaLineBreakpoint) {
				if(!thread.isPerformingEvaluation()) { // TODO nao funciona
					try {
						exception = null;
						exceptionFrame = null;
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
						stackView.setError(msg);
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



	//	private void clearView(boolean startMsg) {
	//		exception = null;
	//		exceptionFrame = null;
	//		model = new CallStackModel2();
	//		handleFrames(new IStackFrame[0]);
	//		Display.getDefault().asyncExec(() -> {
	//			label.setText(startMsg ? Constants.START_MESSAGE : "");
	//			area.setBackground(null);
	//		});
	//	}



	private void handleFrames(IStackFrame[] frames) {
		assert frames != null;

		if(stackLayout.topControl != scroll) {
			Display.getDefault().syncExec(() -> {
				stackLayout.topControl = scroll;
				scroll.getParent().layout();
			});
		}
		int unchanged = model.handle(frames);
//		Display.getDefault().syncExec(() -> {
//			Control[] items = frameComposite.getChildren();
//			for(int i = unchanged+1; i < items.length; i++)
//				items[i].dispose();
//		});

		//Display.getDefault().syncExec(() -> clearView(false));

		model.update();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				stackView.updateFrames(model.getStackPath());
				scroll.setOrigin(scroll.getOrigin().x, Integer.MAX_VALUE);
			}
		});
	}



	private class StackView extends Composite {
		double zoom;
		List<FrameView> frames;

		public StackView(Composite parent) {
			super(parent, SWT.NONE);
			setBackground(Constants.WHITE_COLOR);
			setLayout(new GridLayout(1, true));
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			frames = new ArrayList<>();
			zoom = 1.0;
		}

		public void setError(String msg) {
			frames.get(frames.size()-1).setError(msg);
		}

		public void updateFrames(List<StackFrameModel> stackPath) {
			int diff = stackPath.size() - frames.size();
			
			while(diff > 0) {
				frames.add(new FrameView(this));
				diff--;
			}
			while(diff < 0) {
				frames.remove(frames.size()-1).dispose();
				diff++;
			}

			assert stackPath.size() == frames.size();

			for(int i = 0; i < stackPath.size(); i++) {
				StackFrameModel model = stackPath.get(i);
				frames.get(i).setInput(model);
				frames.get(i).setExpanded(i == stackPath.size()-1);
			}
		}

		public void zoomIn() {
			zoom *= 1.05;
			for(FrameView frame : frames)
				frame.setZoom(zoom);
		}

		public void zoomOut() {
			zoom *= .95;
			for(FrameView frame : frames)
				frame.setZoom(zoom);
		}

		public boolean isEmpty() {
			return frames.isEmpty();
		}

		public void copyToClipBoard() {
			if(!frames.isEmpty())
				frames.get(frames.size()-1).copyToClipBoard();
		}
	}


	private class FrameView extends Composite {
		GraphViewerZoomable viewer;
		Composite compositeHeader;
		Composite compositeViewer;
		StackFrameModel model;
		Label header;
		GridData gridData;
		public FrameView(Composite parent) {
			super(parent, SWT.BORDER);
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			GridLayout layout = new GridLayout(1, false);
			setLayout(layout);

			compositeHeader = new Composite(this, SWT.NONE);
			compositeHeader.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
			compositeHeader.setLayout(new RowLayout());
			
			new Label(compositeHeader,SWT.NONE).setImage(image("frame.gif"));
			header = new Label(compositeHeader, SWT.BORDER);
			FontManager.setFont(header, Constants.MESSAGE_FONT_SIZE);
			
			
			gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			compositeViewer = new Composite(this, SWT.NONE);
			compositeViewer.setLayout(new FillLayout());
			compositeViewer.setLayoutData(gridData);
			
			viewer = new GraphViewerZoomable(compositeViewer, SWT.BORDER);
			viewer.setLayoutAlgorithm(new PandionJLayoutAlgorithm());
			viewer.setContentProvider(new NodeProvider());
			viewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
			viewer.setLabelProvider(new FigureProvider(viewer.getGraphControl()));
			viewer.getGraphControl().addControlListener(new ControlAdapter() {
				public void controlResized(ControlEvent e) {
					parent.layout();
				}
			});

			header.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					setExpanded(!isExpanded());
				}
			});

			viewer.addDoubleClickListener(new IDoubleClickListener() {

				@Override
				public void doubleClick(DoubleClickEvent event) {
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

		}

		public void setExpanded(boolean expanded) {
			setLayoutData(new GridData(SWT.FILL, expanded ? SWT.FILL : SWT.DEFAULT, true, expanded));
			gridData.exclude = !expanded;
			compositeViewer.setVisible(expanded);
			getParent().layout();
		}
		
		public boolean isExpanded() {
			return !gridData.exclude;
		}

		public void setZoom(double zoom) {
			viewer.setZoom(zoom);
		}

		public void setError(String message) {
			setBackground(Constants.ERROR_COLOR);
			setToolTipText(message);
		}

		void setInput(StackFrameModel frameModel) {
			if(this.model != frameModel) {
				header.setText(frameModel.toString());
				this.model = frameModel;
				viewer.setInput(frameModel);
				viewer.setLayoutAlgorithm(new PandionJLayoutAlgorithm());
				viewer.getGraphControl().setEnabled(true);
				frameModel.registerDisplayObserver(new Observer() {
					public void update(Observable o, Object e) {
						viewer.refresh();
						viewer.applyLayout();
					}
				}, viewer.getControl());
				getParent().layout();
			}
		}

		void copyToClipBoard() {
			Graph item = viewer.getGraphControl();
			Rectangle size = compositeViewer.getClientArea();
			compositeViewer.setBackground(Constants.HIGHLIGHT_COLOR);
			GC gc = new GC(compositeViewer);
			Rectangle clipping2 = gc.getClipping();
			Image img = new Image(Display.getDefault(), size.width, size.height);
			gc.copyArea(img, 0, 0);
			ImageData imageData = img.getImageData();
			RGB[] rgb = new RGB[256];

			// build grey scale palette: 256 different grey values are generated. 
			for (int i = 0; i < 256; i++) {
			    rgb[i] = new RGB(i, i, i);
			}

			// Construct a new indexed palette given an array of RGB values.
			PaletteData palette = new PaletteData(rgb);
			Image img2 = new Image(Display.getDefault(), new ImageData(size.width, size.height, 8, palette));
			
		    Shell popup = new Shell(Display.getDefault());
	        popup.setText("Image");
	        popup.setBounds(50, 50, 200, 200);
			 Canvas canvas = new Canvas(popup, SWT.NONE);
		        canvas.setBounds(img.getBounds());
		        canvas.addPaintListener(new PaintListener() {
		          public void paintControl(PaintEvent e) {
		            e.gc.drawImage(img2, 0, 0);
		          }
		        });
		        popup.open();
			Clipboard clipboard = new Clipboard(Display.getDefault());
			clipboard.setContents(new Object[]{img2.getImageData()}, new Transfer[]{ ImageTransfer.getInstance()}); 
			img.dispose();
			gc.dispose();
		}
	}


	private void addMenuBarItems() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
		menuManager.add(new Action("highlight color") {
		});
		menuManager.add(new Action("Copy canvas to clipboard") {
			@Override
			public void run() {
				stackView.copyToClipBoard();
			}

			@Override
			public boolean isEnabled() {
				return !stackView.isEmpty();
			}
		});
		
		
	}

	private void addToolbarAction(String name, boolean toggle, String imageName, String description, Runnable runnable) {
		Action a = new Action(name, toggle ? Action.AS_CHECK_BOX : Action.AS_PUSH_BUTTON) {
			public void run() {
				runnable.run();
			}
		};
		a.setImageDescriptor(ImageDescriptor.createFromImage(image(imageName)));
		String tooltip = name;
		if(description != null)
			tooltip += "\n" + description;
		a.setToolTipText(tooltip);
		toolBar.add(a);
	}

}
