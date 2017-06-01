package pt.iscte.pandionj;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.request.MethodExitRequestImpl;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.IJDIEventListener;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.osgi.framework.Bundle;

import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.MethodExitEvent;

import pt.iscte.pandionj.FontManager.Style;
import pt.iscte.pandionj.model.CallStackModel;
import pt.iscte.pandionj.model.StackFrameModel;

// TODO reload everything on view init
public class PandionJView extends ViewPart { 
	private static PandionJView instance;

	private CallStackModel model;
	private IStackFrame exceptionFrame;
	private String exception;

	private IDebugEventSetListener debugEventListener;
	private PandionJBreakpointListener breakpointListener;

	private ScrolledComposite scroll; 
	private Composite area;
	private StackView stackView;

	private Label labelInit;

	private StackLayout stackLayout;

	private Map<String, Image> images;

	private IContextService contextService;


	private IToolBarManager toolBar;

	public PandionJView() {
		instance = this;
		images = new HashMap<>();
	}

	public static PandionJView getInstance() {
		return instance;
	}

	@Override
	public void createPartControl(Composite parent) {
		contextService = (IContextService)PlatformUI.getWorkbench().getService(IContextService.class);

		createWidgets(parent);
		model = new CallStackModel();
		stackView.setInput(model);

		debugEventListener = new DebugListener();
		breakpointListener = new PandionJBreakpointListener(model);

		DebugPlugin.getDefault().addDebugEventListener(debugEventListener);
		JDIDebugModel.addJavaBreakpointListener(breakpointListener);

		toolBar = getViewSite().getActionBars().getToolBarManager();
		addToolbarAction("Run garbage collector", false, Constants.TRASH_ICON, Constants.TRASH_MESSAGE, () -> model.simulateGC());
		addToolbarAction("Zoom in", false, "zoomin.gif", null, () -> stackView.zoomIn());
		addToolbarAction("Zoom out", false, "zoomout.gif", null, () -> stackView.zoomOut());

		//		addToolbarAction("Highlight", true, "highlight.gif", "Activates the highlight mode, which ...", () -> {});
		//		addToolbarAction("Clipboard", false, "clipboard.gif", "Copies the visible area of the top frame as image to the clipboard.", () -> stackView.copyToClipBoard());
	}


	@Override
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().removeDebugEventListener(debugEventListener);
		JDIDebugModel.removeJavaBreakpointListener(breakpointListener);
		for(Image img : images.values())
			img.dispose();

		FontManager.dispose();
	}


	private Image image(String name) {
		Image img = images.get(name);
		if(img == null) {
			Bundle bundle = Platform.getBundle(Constants.PLUGIN_ID);
			URL imagePath = FileLocator.find(bundle, new Path(Constants.IMAGE_FOLDER + "/" + name), null);
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
		scroll.setMinHeight(200);
		scroll.setMinWidth(400);

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
		labelInit = new Label(labelComposite, SWT.WRAP);
		FontManager.setFont(labelInit, Constants.MESSAGE_FONT_SIZE, Style.ITALIC);
		labelInit.setText(Constants.START_MESSAGE);
		labelInit.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		stackLayout.topControl = labelComposite;

		new StaticArea(area);
		stackView = new StackView(area);
	}

	@Override
	public void setFocus() {
		scroll.setFocus();
		contextService.activateContext("pt.iscte.pandionj.context");
	}

	private class DebugListener implements IDebugEventSetListener {
		public void handleDebugEvents(DebugEvent[] events) {
			if(events.length > 0) {
				DebugEvent e = events[0];
				if(e.getKind() == DebugEvent.SUSPEND && e.getDetail() == DebugEvent.STEP_END && exception == null) {
					IJavaThread thread = (IJavaThread) e.getSource();
					executeInternal(() -> {
						if(thread.getTopStackFrame().getLineNumber() == -1)
							thread.resume();

						handleFrames(thread.getStackFrames());
					});
				}
				else if(e.getKind() == DebugEvent.RESUME && e.getDetail() == DebugEvent.STEP_INTO) {
					breakpointListener.enableFilter();
				}
				else if(e.getKind() == DebugEvent.RESUME &&
						(
								e.getDetail() == DebugEvent.STEP_OVER || 
								e.getDetail() == DebugEvent.STEP_RETURN || 
								e.getDetail() == DebugEvent.CLIENT_REQUEST 
								))  {
					breakpointListener.disableFilter();
				}
				else if(e.getKind() == DebugEvent.TERMINATE) {
					model.setTerminated();
				}
			}
		}
	}




	void handleLinebreakPoint(IJavaThread thread) {
		executeInternal(() -> {
			exception = null;
			exceptionFrame = null;
			IStackFrame[] frames = thread.getStackFrames(); 
			handleFrames(frames);
		});
	}

	void handleExceptionBreakpoint(IJavaThread thread, IJavaExceptionBreakpoint exceptionBreakPoint) {
		executeInternal(() -> {
			thread.terminateEvaluation();
			exception = exceptionBreakPoint.getExceptionTypeName();
			exceptionFrame = thread.getTopStackFrame();
			IStackFrame[] frames = thread.getStackFrames(); 
			handleFrames(frames);
			if(!model.isEmpty()) {
				StackFrameModel frame = model.getFrame(exceptionFrame);
				int line = exceptionFrame.getLineNumber();
				frame.processException(exception, line);  
			}
		}); 
	}


	private void handleFrames(IStackFrame[] frames) {
		assert frames != null;

		if(stackLayout.topControl != scroll) {
			Display.getDefault().syncExec(() -> {
				stackLayout.topControl = scroll;
				scroll.getParent().layout();
			});
		}
		model.update(frames);
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

	private void addToolbarAction(String name, boolean toggle, String imageName, String description, Action action) {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
		action.setImageDescriptor(ImageDescriptor.createFromImage(image(imageName)));
		String tooltip = name;
		if(description != null)
			tooltip += "\n" + description;
		action.setToolTipText(tooltip);
		menuManager.add(action);
	}

	private void addToolbarAction(String name, boolean toggle, String imageName, String description, Runnable runnable) {
		Action a = new Action(name, toggle ? Action.AS_CHECK_BOX : Action.AS_PUSH_BUTTON) {
			public void run() {
				runnable.run();
			}
		};
		addToolbarAction(name, toggle, imageName, description, a);
	}

	public interface DebugOperation<T> {
		T run() throws DebugException;
	}

	public interface DebugRun {
		void run() throws DebugException;
	}

	public static <T> T execute(DebugOperation<T> r, T defaultValue)  {
		return instance.executeInternal(r, defaultValue);
	}

	public static void execute(DebugRun r) {
		instance.executeInternal(r);
	}

	public static void executeUpdate(DebugRun r) {
		Display.getDefault().asyncExec(() -> instance.executeInternal(r));
	}

	private void executeInternal(DebugRun r) {
		try {
			r.run();
		}
		catch(DebugException e) {
			model.setTerminated();
		}
	}

	private <T> T executeInternal(DebugOperation<T> r, T defaultValue) {
		try {
			return r.run();
		}
		catch(DebugException e) {
			model.setTerminated();
			return null;
		}
	}



	



	//	private IDebugContextListener debugUiListener;
	//	debugUiListener = new DebugUIListener();
	//	DebugUITools.getDebugContextManager().addDebugContextListener(debugUiListener);
	//	DebugUITools.getDebugContextManager().removeDebugContextListener(debugUiListener);


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


}
