package pt.iscte.pandionj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.ModelObserver;
import pt.iscte.pandionj.extensibility.PandionJConstants;
import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.figures.ObjectContainer;
import pt.iscte.pandionj.figures.PandionJFigure;
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
	private Multimap<Object, PolylineConnection> pointersMapOwners;
	private List<ObjectContainer> objectContainers;

	private MenuItem clearItem;

	RuntimeViewer(Composite parent) {
		super(parent, SWT.BORDER);
		instance = this;

		setLayout(new FillLayout());
		setBackground(PandionJConstants.Colors.VIEW_BACKGROUND);
		scroll = new ScrolledComposite(this, SWT.H_SCROLL | SWT.V_SCROLL);
		scroll.setBackground(PandionJConstants.Colors.VIEW_BACKGROUND);
		canvas = new Canvas(scroll, SWT.DOUBLE_BUFFERED);
		canvas.setBackground(ColorConstants.white);
		canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		scroll.setContent(canvas);
		addMenu();

		//				rootFig = new ScalableLayeredPane();
		//				((ScalableLayeredPane) rootFig).setScale(2);
		rootFig = new Figure();
		rootFig.setOpaque(true);
		rootGrid = new GridLayout(2, false);
		rootGrid.horizontalSpacing = PandionJConstants.STACK_TO_OBJECTS_GAP;
		rootGrid.marginWidth = PandionJConstants.MARGIN;
		rootGrid.marginHeight = PandionJConstants.MARGIN;
		rootFig.setLayoutManager(rootGrid);

		stackFig = new StackContainer();
		rootFig.add(stackFig);
		org.eclipse.draw2d.GridData d = new org.eclipse.draw2d.GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
		d.widthHint = Math.max(PandionJConstants.STACKCOLUMN_MIN_WIDTH, stackFig.getPreferredSize().width);
		rootGrid.setConstraint(stackFig, d);

		objectContainers = new ArrayList<>();
		objectContainer = ObjectContainer.create(true);
		rootFig.add(objectContainer);
		rootGrid.setConstraint(objectContainer, new org.eclipse.draw2d.GridData(SWT.FILL, SWT.FILL, true, true));

		lws = new LightweightSystem(canvas);
		lws.setContents(rootFig);
		pointersMap = new HashMap<>();
		pointersMapOwners = ArrayListMultimap.create();

	}

	public static RuntimeViewer getInstance() {
		return instance;
	}

	public FigureProvider getFigureProvider() {
		return figProvider;
	}

	public void setInput(RuntimeModel model) {
		figProvider = new FigureProvider(model);
		model.registerObserver((e) -> refresh(model, e));
	}

	private void refresh(RuntimeModel model, RuntimeModel.Event<?> event) {
		PandionJUI.executeUpdate(() -> {
			if(event.type == RuntimeModel.Event.Type.NEW_STACK)
				rebuildStack(model);
			else if(event.type == RuntimeModel.Event.Type.REMOVE_FRAME) {
				StackFrameModel f = (StackFrameModel) event.arg;
				stackFig.removeFrame(f);

				List<?> children = rootFig.getChildren();
				for (IReferenceModel v : f.getReferenceVariables()) {
					PolylineConnection c = pointersMap.remove(v);
					if(c != null && children.contains(c))
						rootFig.remove(c);
				}
			}
			else if(event.type == RuntimeModel.Event.Type.NEW_FRAME) {
				StackFrameModel frame = (StackFrameModel) event.arg;
				if(!frame.isInstance())
					stackFig.addFrame(frame, this, objectContainer, false);
			}
			stackFig.getLayoutManager().layout(stackFig);
			updateLayout();

			clearItem.setEnabled(model.isTerminated());
		});
	}


	private void rebuildStack(RuntimeModel model) {
		clear();
		IStackFrameModel staticVars = model.getStaticVars();
		stackFig.addFrame(staticVars, this, objectContainer, true);
	}

	private void clear() {
		for(PolylineConnection p : pointersMap.values())
			rootFig.remove(p);

		pointersMap.clear();
		pointersMapOwners.clear();
		stackFig.removeAll();
		objectContainer.removeAll();
	}


	public void updateLayout() {
		if(!canvas.isDisposed()) {
			Dimension size = rootFig.getPreferredSize();
			canvas.setSize(size.width, size.height);
			org.eclipse.draw2d.GridData d = (org.eclipse.draw2d.GridData) rootGrid.getConstraint(stackFig);
			d.widthHint = Math.max(PandionJConstants.STACKCOLUMN_MIN_WIDTH, stackFig.getPreferredSize().width);
			rootGrid.layout(rootFig);
			requestLayout();
		}
	}


	private void addPointer(IReferenceModel ref, PolylineConnection pointer, Object owner) {
		assert pointer != null;
		rootFig.add(pointer);
		pointersMap.put(ref, pointer);
		pointersMapOwners.put(owner, pointer);
		//		pointer.setToolTip(new Label(ref.toString()));
	}


	public void addPointer(IReferenceModel ref, ConnectionAnchor sourceAnchor, ConnectionAnchor targetAnchor, ObjectContainer container, Object owner) {
		assert ref != null;
		IEntityModel target = ref.getModelTarget();
		PolylineConnection pointer = new PolylineConnection();
		pointer.setVisible(!target.isNull());
		pointer.setSourceAnchor(sourceAnchor);
		pointer.setTargetAnchor(target.isNull() ? sourceAnchor : targetAnchor);
		Utils.addArrowDecoration(pointer);
		addPointerObserver(ref, pointer, container);
		addPointer(ref, pointer, owner);
	}

	static void addPointerObserver(IReferenceModel ref, PolylineConnection pointer, ObjectContainer container) {
		ref.registerDisplayObserver(new ModelObserver<IEntityModel>() {
			@Override
			public void update(IEntityModel arg) {
				IEntityModel target = ref.getModelTarget();
				pointer.setVisible(!target.isNull());
				if(!target.isNull()) {
					PandionJFigure<?> targetFig = container.addObject(target, ref.getIndex());
					pointer.setTargetAnchor(targetFig.getIncommingAnchor());
					Utils.addArrowDecoration(pointer);
				}
			}
		});
	}

	public void showPointer(IReferenceModel ref, boolean show) {
		PolylineConnection p = pointersMap.get(ref);
		if(p != null)
			p.setVisible(show && !ref.getModelTarget().isNull());
	}

	public void showPointers(Object owner, boolean show) {
		for(PolylineConnection pointer : pointersMapOwners.get(owner))
			pointer.setVisible(show);
	}

	public void removePointer(IReferenceModel ref) {
		PolylineConnection p = pointersMap.remove(ref);
		if(p != null)
			rootFig.remove(p);
	}

	public void removePointers(Object owner) {
		for(PolylineConnection pointer : pointersMapOwners.get(owner))
			rootFig.remove(pointer);

		pointersMapOwners.removeAll(owner);
	}


	public void addObject(IEntityModel e) {
		PandionJUI.executeUpdate(() -> {
			objectContainer.addObject(e);
			updateLayout();
		});
	}

	public void addObjectContainer(ObjectContainer c) {
		objectContainers.add(c);
	}

	public List<ObjectContainer> getObjectContainers() {
		return Collections.unmodifiableList(objectContainers);
	}

	private void addMenu() {
		Menu menu = new Menu(canvas);
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText(PandionJConstants.Messages.COPY_CLIPBOARD);
		item.setImage(PandionJUI.getImage("clipboard.gif"));
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				copyToClipBoard();
			}
		});


		MenuItem setArrayItem = new MenuItem(menu, SWT.PUSH);
		setArrayItem.setText(PandionJConstants.Messages.SET_ARRAY_MAX + " (current: " + PandionJView.getMaxArrayLength() + ")");
		setArrayItem.setImage(PandionJUI.getImage("array.gif"));
		setArrayItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Display display = Display.getDefault();
				Shell shell = new Shell(display, SWT.APPLICATION_MODAL);
				shell.setLayout(new FillLayout());
				shell.setLocation(Display.getDefault().getCursorLocation());
				//				Label label = new Label(shell, SWT.NONE);
				//				label.setText("Maximum array length display");
				Text text = new Text(shell, SWT.BORDER);
				text.setText(PandionJView.getMaxArrayLength() + "");
				text.addVerifyListener(new VerifyListener() {
					@Override
					public void verifyText(VerifyEvent e) {
						if(!(e.character >= '0' && e.character <= '9') && e.keyCode != SWT.BS)
							e.doit = false;
					}
				});
				text.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if(e.keyCode == SWT.CR) {
							int val = Integer.parseInt(text.getText());
							PandionJView.setArrayMaximumLength(val);
							setArrayItem.setText(PandionJConstants.Messages.SET_ARRAY_MAX + " (current: " + val + ")");
							shell.close();
						}
						else if(e.keyCode == SWT.ESC) 
							shell.close();
					}
				});
				shell.pack();
				shell.open();
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			}
		});

		clearItem = new MenuItem(menu, SWT.PUSH);
		clearItem.setText(PandionJConstants.Messages.CLEAR);
		clearItem.setImage(PandionJUI.getImage("clear.gif"));
		clearItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clear();
			}
		});
		canvas.setMenu(menu);
	}


	//	void saveImage() {
	//		ImageLoader imageLoader = new ImageLoader();
	//		imageLoader.data = new ImageData[] {ideaImageData};
	//		imageLoader.save("C:/temp/Idea_PureWhite.jpg",SWT.IMAGE_JPEG); 
	//	}

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
