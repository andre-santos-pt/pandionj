package pt.iscte.pandionj;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.figures.AbstractArrayFigure;
import pt.iscte.pandionj.figures.ArrayReferenceFigure;
import pt.iscte.pandionj.figures.IllustrationBorder;
import pt.iscte.pandionj.figures.ObjectContainer;
import pt.iscte.pandionj.figures.PandionJFigure;
import pt.iscte.pandionj.figures.PandionJFigure.Extension;
import pt.iscte.pandionj.model.ModelObserver;
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

		objectFig = new ObjectContainer();
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

	public FigureProvider getFigureProvider() {
		return figProvider;
	}
	
	
	public void setInput(RuntimeModel model) {
//		PandionJUI.executeUpdate(() -> rebuildStack(model));
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
				StackFrameFigure sv = new StackFrameFigure(RuntimeViewer.this, frame, objectFig, invisible);
				add(sv);
				getLayoutManager().setConstraint(sv, new org.eclipse.draw2d.GridData(SWT.FILL, SWT.DEFAULT, true, false));
			}
		}
	}






//	 class ObjectContainer extends Figure {
//
//		class Container2d extends Figure {
//			
//			Container2d(IArrayModel<?> a) {
//				setOpaque(true);
//				setLayoutManager(new GridLayout(1, false));
//				
//				Iterator<Integer> it = a.getValidModelIndexes(); 
//				while(it.hasNext()) {
//					add(new Label());
//					it.next();
//				}
//			}
//			
//			public void addAt(int index, IFigure figure) {
//				int i = Math.min(index, Constants.ARRAY_LENGTH_LIMIT-1);
//				List children = getChildren();
//				remove((IFigure) children.get(i)); 
//				add(figure, i);
//			}
//		}
//		
//		
//		ObjectContainer() {
//			setBackgroundColor(ColorConstants.white);
//			setOpaque(true);
//			setLayoutManager(new GridLayout(1, true));
//		}
//
//		void setInput(RuntimeModel model) {
//			PandionJUI.executeUpdate(() -> removeAll());
//			model.registerDisplayObserver((e) -> {
//				if(e.type == RuntimeModel.Event.Type.NEW_STACK)
//					removeAll();
//				else if(e.type == RuntimeModel.Event.Type.NEW_OBJECT)
//					addObject((IEntityModel) e.arg);
//			});
//		}
//
//		PandionJFigure<?> addObject(IEntityModel e) {
//			PandionJFigure<?> fig = figProvider.getFigure(e);
//			if(!containsChild(fig)) {
//				if(e instanceof IArrayModel && ((IArrayModel<?>) e).isReferenceType() && fig instanceof ArrayReferenceFigure) {
//					Extension ext = new Extension(fig, e);
//					GridLayout gridLayout = new GridLayout(2, false);
//					gridLayout.horizontalSpacing = Constants.STACK_TO_OBJECTS_GAP;
//					ext.setLayoutManager(gridLayout);
//					org.eclipse.draw2d.GridData alignTop = new org.eclipse.draw2d.GridData(SWT.LEFT, SWT.TOP, false, false);
//					gridLayout.setConstraint(fig, alignTop);
//					
//					Container2d container2d = new Container2d((IArrayModel<?>) e);
//					ext.add(container2d);
////					gridLayout.setConstraint(container2d, alignTop);
//
//					IArrayModel<IReferenceModel> a = (IArrayModel<IReferenceModel>) e;
//					Iterator<Integer> it = a.getValidModelIndexes();
//					while(it.hasNext()) {
//						Integer i = it.next();
//						add2dElement(fig, a, i, container2d);
//					}
//					add(ext);
//				}
//				else {					
//					add(fig);
//				}
//				getLayoutManager().layout(this);
//			}
//			return fig;
//		}
//
//		private void add2dElement(PandionJFigure<?> targetFig, IArrayModel<IReferenceModel> a, int i, Container2d container) {
//			IReferenceModel e = a.getElementModel(i);
//			IEntityModel eTarget = e.getModelTarget();
//			PandionJFigure<?> eTargetFig = null;
//			if(!eTarget.isNull()) {
//				eTargetFig = figProvider.getFigure(eTarget);
//				container.addAt(i, eTargetFig);
//			}
//			addPointer2D((ArrayReferenceFigure) targetFig, e, i, eTarget, eTargetFig, container);
//		}
//
//
//		private void addPointer2D(ArrayReferenceFigure figure, IReferenceModel ref, int index, IEntityModel target, PandionJFigure<?> targetFig, Container2d container) {
//			PolylineConnection pointer = new PolylineConnection();
//			pointer.setVisible(!target.isNull());
//			pointer.setSourceAnchor(figure.getAnchor(index));
//			if(target.isNull())
//				pointer.setTargetAnchor(figure.getAnchor(index));
//			else
//				pointer.setTargetAnchor(targetFig.getIncommingAnchor());
//
//			Utils.addArrowDecoration(pointer);
//			addPointerObserver2d(ref, pointer, container, index);
//			addPointer(ref, pointer);
//		}
//
//		private void addPointerObserver2d(IReferenceModel ref, PolylineConnection pointer, Container2d container, int index) {
//			ref.registerDisplayObserver(new ModelObserver<IEntityModel>() {
//				@Override
//				public void update(IEntityModel arg) {
//					IEntityModel target = ref.getModelTarget();
//					pointer.setVisible(!target.isNull());
//					if(!target.isNull()) {
//						PandionJFigure<?> figure = figProvider.getFigure(target);
//						container.addAt(index, figure);
//						pointer.setTargetAnchor(figure.getIncommingAnchor());
//						Utils.addArrowDecoration(pointer);
//						container.getLayoutManager().layout(container);
//					}
//				}
//			});
//		}
//
//		void updateIllustration(IReferenceModel v, ExceptionType exception) {
//			IEntityModel target = v.getModelTarget();
//			PandionJFigure<?> fig = getDeepChild(target);
//			if(fig != null) {
//				if(handleIllustration(v, fig.getInnerFigure(), exception)) {
//					//								xyLayout.setConstraint(fig, new Rectangle(fig.getBounds().getLocation(), fig.getPreferredSize()));
//					//								xyLayout.layout(StackFrameViewer.this);
//				}
//
//				if(target instanceof IArrayModel && ((IArrayModel) target).isReferenceType()) {
//					IArrayModel<IReferenceModel> a = (IArrayModel<IReferenceModel>) target;
//					for (IReferenceModel e : a.getModelElements())
//						updateIllustration(e, exception);
//				}
//			}
//		}
//
//		
//		private boolean handleIllustration(IReferenceModel reference, IFigure targetFig, ExceptionType exception) {
//			if(targetFig instanceof AbstractArrayFigure) {
//				if(reference.hasIndexVars()) {
//					IllustrationBorder b = new IllustrationBorder(reference, (AbstractArrayFigure<?>) targetFig, exception);
//					targetFig.setBorder(b);
//					return true;
//				}
//				else
//					targetFig.setBorder(new MarginBorder(new Insets(0, Constants.POSITION_WIDTH, 20, Constants.POSITION_WIDTH))); // TODO temp margin
//			}
//			return false;
//		} 
//
//		private 	boolean containsChild(IFigure child) {
//			for (Object object : getChildren()) {
//				if(object == child)
//					return true;
//			}
//			return false;
//		}
//
//		private PandionJFigure<?> getDeepChild(IEntityModel e) {
//			return getDeepChildRef(this, e);
//		}
//
//		private PandionJFigure<?> getDeepChildRef(IFigure f, IEntityModel e) {
//			for (Object object : f.getChildren()) {
//				if(object instanceof PandionJFigure && ((PandionJFigure<?>) object).getModel() == e)
//					return (PandionJFigure<?>) object;
//
//				PandionJFigure<?> ret = getDeepChildRef((IFigure) object, e);
//				if(ret != null)
//					return ret;
//			}
//			return null;
//		}
//	}

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
