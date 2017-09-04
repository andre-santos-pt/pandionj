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

import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.figures.ObjectContainer;
import pt.iscte.pandionj.figures.StackFrameFigure;
import pt.iscte.pandionj.model.RuntimeModel;
import pt.iscte.pandionj.model.StackFrameModel;

public class RuntimeViewer extends Composite {
	private static RuntimeViewer instance = null;
	
	private FigureProvider figProvider;
	private Figure rootFig;
	private StackFigure stackFig;
	private ObjectContainer objectFig;
	private LightweightSystem lws;
	private GridLayout rootGrid;
	private ScrolledComposite scroll;
	private Canvas canvas;
	private Map<IReferenceModel, PolylineConnection> pointersMap;

	RuntimeViewer(Composite parent) {
		super(parent, SWT.BORDER);
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

		stackFig = new StackFigure();
		rootFig.add(stackFig);
		org.eclipse.draw2d.GridData d = new org.eclipse.draw2d.GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
		d.widthHint = Constants.STACKCOLUMN_MIN_WIDTH;
		rootGrid.setConstraint(stackFig, d);

		objectFig = new ObjectContainer(true);
		rootFig.add(objectFig);
		rootGrid.setConstraint(objectFig, new org.eclipse.draw2d.GridData(SWT.FILL, SWT.FILL, true, true));

		lws = new LightweightSystem(canvas);
		lws.setContents(rootFig);
		
		pointersMap = new HashMap<>();
		
		instance = this;
	}

	public static RuntimeViewer getInstance() {
		return instance;
	}
	
	public FigureProvider getFigureProvider() {
		return figProvider;
	}
	
	public void setInput(RuntimeModel model) {
		figProvider = new FigureProvider(model);
		objectFig.setFigProvider(figProvider);
		model.registerDisplayObserver((e) -> refresh(model, e));
	}

	private void refresh(RuntimeModel model, RuntimeModel.Event<?> event) {
		if(event.type == RuntimeModel.Event.Type.NEW_STACK)
			rebuildStack(model);
		else if(event.type == RuntimeModel.Event.Type.NEW_FRAME)
			stackFig.addFrame((StackFrameModel) event.arg, false);

		stackFig.getLayoutManager().layout(stackFig);
		updateLayout();
	}

	private void updateLayout() {
		org.eclipse.swt.graphics.Point prev = canvas.getSize();
		Dimension size = rootFig.getPreferredSize();
		canvas.setSize(size.width, size.height);
		canvas.layout();
		if(size.height > prev.y)
			scroll.setOrigin(0, size.height);
		
		org.eclipse.draw2d.GridData d = (org.eclipse.draw2d.GridData) rootGrid.getConstraint(stackFig);
		d.widthHint = stackFig.getPreferredSize().width;
		rootGrid.layout(rootFig);

	}

	
	private void rebuildStack(RuntimeModel model) {
		for(PolylineConnection p : pointersMap.values())
			rootFig.remove(p);
		
		pointersMap.clear();
		
		stackFig.removeAll();
		objectFig.removeAll();
		IStackFrameModel staticVars = model.getStaticVars();
		stackFig.addFrame(staticVars, true);
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




	class StackFigure extends Figure {
		public StackFigure() {
			GridLayout gridLayout = new GridLayout(1, true);
			gridLayout.verticalSpacing = Constants.OBJECT_PADDING*2;
			setLayoutManager(gridLayout);
			setOpaque(true);
		}

		void addFrame(IStackFrameModel frame, boolean invisible) {
			if(frame.getLineNumber() != -1) {
				StackFrameFigure sv = new StackFrameFigure(RuntimeViewer.this, frame, objectFig, invisible, false);
				add(sv);
				getLayoutManager().setConstraint(sv, new org.eclipse.draw2d.GridData(SWT.FILL, SWT.DEFAULT, true, false));
			}
		}
	}



	
	private void addMenu() {
		Menu menu = new Menu(this);
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText("Copy image");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				copyToClipBoard();
			}
		});
		setMenu(menu);
	}


	void copyToClipBoard() {
		Dimension size = rootFig.getPreferredSize();
		Image image = new Image(Display.getDefault(), size.width, size.height);
		GC gc = new GC(image);
		SWTGraphics graphics = new SWTGraphics(gc);
		rootFig.paint(graphics);
		
//		Composite item = viewer;
//		Point p = viewer.computeSize(SWT.DEFAULT, SWT.DEFAULT);
//
//		Rectangle size = item.getClientArea();
//
//		System.out.println(p +  "    " + size);
//		//			compositeViewer.setBackground(Constants.HIGHLIGHT_COLOR);
//		GC gc = new GC(item);
//		//			Rectangle clipping2 = gc.getClipping();
//		//			Image img = new Image(Display.getDefault(), size.width, size.height);
//		//			gc.copyArea(img, 0, 0);
//		//			ImageData imageData = img.getImageData();
//
//		RGB[] rgb = new RGB[256];
//		// build grey scale palette: 256 different grey values are generated. 
//		for (int i = 0; i < 256; i++) {
//			rgb[i] = new RGB(i, i, i);
//		}
//
//		// Construct a new indexed palette given an array of RGB values.
//		PaletteData palette = new PaletteData(rgb);
//		Image img2 = new Image(Display.getDefault(), new ImageData(size.width, size.height, 8, palette));
//		//			gc.setClipping(0, 0, p.x, p.y);
//		gc.copyArea(img2, 0, 0);
//		Shell popup = new Shell(Display.getDefault());
//		popup.setText("Image");
//		popup.setBounds(50, 50, 200, 200);
//		Canvas canvas = new Canvas(popup, SWT.NONE);
//		canvas.setBackground(new Color(null,255,0,0));
//		canvas.setBounds(image.getBounds());
//		canvas.addPaintListener(new PaintListener() {
//			public void paintControl(PaintEvent e) {
//				e.gc.drawImage(image, 0, 0);
//			}
//		});
//		popup.open();
		Clipboard clipboard = new Clipboard(Display.getDefault());
		clipboard.setContents(new Object[]{image.getImageData()}, new Transfer[]{ ImageTransfer.getInstance()}); 
		image.dispose();
		gc.dispose();
	}

	
}
