package pt.iscte.pandionj;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.figures.ObjectContainer;
import pt.iscte.pandionj.figures.ObjectFigure;
import pt.iscte.pandionj.figures.StackContainer;
import pt.iscte.pandionj.model.RuntimeModel;
import pt.iscte.pandionj.model.StackFrameModel;

public class RuntimeViewer extends Composite {
	private static RuntimeViewer instance = null;
	
	private FigureProvider figProvider;
	private Figure rootFig;
	private StackContainer stackFig;
	private ObjectContainer objectContainer;
	private LightweightSystem lws;
	private GridLayout rootGrid;
	private ScrolledComposite scroll;
	private Canvas canvas;
	private Map<IReferenceModel, PolylineConnection> pointersMap;

	RuntimeViewer(Composite parent) {
		super(parent, SWT.BORDER);
		instance = this;

		setLayout(new FillLayout());
		setBackground(Constants.Colors.VIEW_BACKGROUND);
		scroll = new ScrolledComposite(this, SWT.H_SCROLL | SWT.V_SCROLL);
		scroll.setBackground(Constants.Colors.VIEW_BACKGROUND);
		canvas = new Canvas(scroll, SWT.DOUBLE_BUFFERED);
		canvas.setBackground(ColorConstants.white);
		canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		scroll.setContent(canvas);
		addMenu();

		rootFig = new Figure();
		rootFig.setOpaque(true);
		rootFig.setBackgroundColor(Constants.Colors.VIEW_BACKGROUND);
		rootGrid = new GridLayout(2, false);
		rootGrid.horizontalSpacing = Constants.STACK_TO_OBJECTS_GAP;
		rootGrid.marginWidth = Constants.MARGIN;
		rootGrid.marginHeight = Constants.MARGIN;
		rootFig.setLayoutManager(rootGrid);

		stackFig = new StackContainer();
		rootFig.add(stackFig);
		org.eclipse.draw2d.GridData d = new org.eclipse.draw2d.GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
		d.widthHint = Math.max(Constants.STACKCOLUMN_MIN_WIDTH, stackFig.getPreferredSize().width);
		rootGrid.setConstraint(stackFig, d);

		objectContainer = new ObjectContainer(true);
		rootFig.add(objectContainer);
		rootGrid.setConstraint(objectContainer, new org.eclipse.draw2d.GridData(SWT.FILL, SWT.FILL, true, true));

		lws = new LightweightSystem(canvas);
		lws.setContents(rootFig);
		
		pointersMap = new HashMap<>();
	}

	public static RuntimeViewer getInstance() {
		return instance;
	}
	
	public FigureProvider getFigureProvider() {
		return figProvider;
	}
	
	public void setInput(RuntimeModel model) {
		figProvider = new FigureProvider(model);
		objectContainer.setFigProvider(figProvider);
		model.registerObserver((e) -> refresh(model, e));
	}

	private void refresh(RuntimeModel model, RuntimeModel.Event<?> event) {
		if(event.type == RuntimeModel.Event.Type.NEW_STACK)
			rebuildStack(model);
		else if(event.type == RuntimeModel.Event.Type.NEW_FRAME) {
			StackFrameModel frame = (StackFrameModel) event.arg;
			if(frame.isInstance()) {
				IObjectModel obj = frame.getThis();
				ObjectFigure fig = objectContainer.findObject(obj);
				if(fig != null)
					fig.addFrame(frame);
			}				
			else {
				stackFig.addFrame(frame, this, objectContainer, false);
			}

		}
		stackFig.getLayoutManager().layout(stackFig);
		updateLayout();
	}

	
	private void rebuildStack(RuntimeModel model) {
		for(PolylineConnection p : pointersMap.values())
			rootFig.remove(p);
		
		pointersMap.clear();
		
		stackFig.removeAll();
		objectContainer.removeAll();
		IStackFrameModel staticVars = model.getStaticVars();
		stackFig.addFrame(staticVars, this, objectContainer, true);
	}
	
	
	private void updateLayout() {
		org.eclipse.swt.graphics.Point prev = canvas.getSize();
		Dimension size = rootFig.getPreferredSize();
		canvas.setSize(size.width, size.height);
		canvas.layout();
		if(size.height > prev.y)
			scroll.setOrigin(0, size.height);
		
		org.eclipse.draw2d.GridData d = (org.eclipse.draw2d.GridData) rootGrid.getConstraint(stackFig);
		d.widthHint = Math.max(Constants.STACKCOLUMN_MIN_WIDTH, stackFig.getPreferredSize().width);
		rootGrid.layout(rootFig);

	}
	
	
	public void addPointer(IReferenceModel ref, PolylineConnection pointer) {
		assert pointer != null;
		rootFig.add(pointer);
		pointersMap.put(ref, pointer);
	}
	
	public void removePointer(IReferenceModel ref) {
		PolylineConnection p = pointersMap.get(ref);
		if(p != null)
			rootFig.remove(p);
	}




//	class StackFigure extends Figure {
//		public StackFigure() {
//			GridLayout gridLayout = new GridLayout(1, true);
//			gridLayout.verticalSpacing = Constants.OBJECT_PADDING*2;
//			setLayoutManager(gridLayout);
//			setOpaque(true);
//		}
//
//		void addFrame(IStackFrameModel frame, boolean invisible) {
//			if(frame.getLineNumber() != -1) {
//				StackFrameFigure sv = new StackFrameFigure(RuntimeViewer.this, frame, objectFig, invisible, false);
//				add(sv);
//				getLayoutManager().setConstraint(sv, new org.eclipse.draw2d.GridData(SWT.FILL, SWT.DEFAULT, true, false));
//			}
//		}
//	}



	
	private void addMenu() {
		Menu menu = new Menu(canvas);
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText(Constants.Messages.COPY_CLIPBOARD);
		item.setImage(PandionJUI.getImage("clipboard.gif"));
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				copyToClipBoard();
			}
		});
		canvas.setMenu(menu);
	}


	void copyToClipBoard() {
		Dimension size = rootFig.getPreferredSize();
		Image image = new Image(Display.getDefault(), size.width, size.height);
		GC gc = new GC(image);
		SWTGraphics graphics = new SWTGraphics(gc);
		rootFig.paint(graphics);
		Clipboard clipboard = new Clipboard(Display.getDefault());
		clipboard.setContents(new Object[]{image.getImageData()}, new Transfer[]{ ImageTransfer.getInstance()}); 
		image.dispose();
		gc.dispose();
	}

	
}
