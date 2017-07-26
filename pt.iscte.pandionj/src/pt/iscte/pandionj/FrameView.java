package pt.iscte.pandionj;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.GestureEvent;
import org.eclipse.swt.events.GestureListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.ZestStyles;

import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.model.StackFrameModel;
import pt.iscte.pandionj.model.VariableModel;

class FrameView extends Composite {
	GraphViewerZoomable viewer;
	Composite compositeHeader;
	Composite compositeViewer;
	StackFrameModel model;
	Label header;
	GridData gridData;
	Slider slider;


	public FrameView(Composite parent) {
		super(parent, SWT.BORDER);
		GridLayout layout = new GridLayout(1, false);
		setLayout(layout);

		compositeHeader = new Composite(this, SWT.NONE);
		compositeHeader.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		compositeHeader.setLayout(new GridLayout(1, true));

		header = new Label(compositeHeader, SWT.BORDER);
		header.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		FontManager.setFont(header, Constants.MESSAGE_FONT_SIZE);

		

		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		compositeViewer = new Composite(this, SWT.NONE);
		compositeViewer.setLayout(new GridLayout());
		compositeViewer.setLayoutData(gridData);

		viewer = new GraphViewerZoomable(compositeViewer, SWT.BORDER);
		viewer.setContentProvider(new NodeProvider());
		viewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);

		viewer.getGraphControl().setLayoutData(new GridData(parent.getBounds().width - Constants.MARGIN, Constants.MARGIN));
		viewer.getGraphControl().setEnabled(true);
		viewer.getGraphControl().setScrollBarVisibility(SWT.VERTICAL);
		viewer.getGraphControl().setTouchEnabled(false);
		viewer.getGraphControl().addGestureListener(new GestureListener() {
			public void gesture(GestureEvent e) {
				if(e.detail == SWT.GESTURE_MAGNIFY)
					viewer.zoom(e.magnification);
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				//					navigateToLine(model.getSourceFile(), model.getLineNumber());
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


		header.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				//				setExpanded(!isExpanded());
				//				getParent().layout();
			}
		});
		// TODO CTRL +-
		//		viewer.getGraphControl().addKeyListener(new KeyListener() {
		//			
		//			@Override
		//			public void keyReleased(KeyEvent e) {
		//				
		//			}
		//			
		//			@Override
		//			public void keyPressed(KeyEvent e) {
		//			}
		//		});

		slider = new Slider(this, SWT.HORIZONTAL | SWT.BORDER);
		slider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		slider.setMinimum(1);
		slider.setMaximum(1);
		slider.setIncrement(1);
		slider.addSelectionListener(new SelectionAdapter() {
			int sel = slider.getSelection();
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(slider.getSelection() != sel) {
					model.setStep(slider.getSelection()-1);
					sel = slider.getSelection();
					PandionJUI.navigateToLine(model.getSourceFile(), model.getStepLine());
					//				slider.setToolTipText(slider.getSelection() + "/" + slider.getMaximum());
				}
			}
		});
		slider.setVisible(false);
	}


	public void setExpanded(boolean expanded) {
		setLayoutData(new GridData(SWT.FILL, expanded ? SWT.FILL : SWT.DEFAULT, true, expanded));
		gridData.exclude = !expanded;
		compositeViewer.setVisible(expanded);
		compositeViewer.requestLayout();
	}

	@Override
	public void dispose() {
		super.dispose();
		model.unregisterObserver(stackObserver);
		model.getRuntime().unregisterObserver(runtimeObserver);
	}

	public boolean isExpanded() {
		return !gridData.exclude;
	}

	public void setZoom(double zoom) {
		//		viewer.setZoom(zoom);
	}

	private void setError(String message) {
		setBackground(Constants.Colors.ERROR);
		header.setToolTipText(message);
	}




	void setInput(StackFrameModel frameModel) {
		Color c = frameModel.isObsolete() ? ColorManager.getColor(150, 150, 150) : null;
		setBackground(c);

		header.setToolTipText("");

		if(this.model != frameModel) {
			this.model = frameModel;
			this.model.registerDisplayObserver(stackObserver);
			this.model.getRuntime().registerDisplayObserver(runtimeObserver);
			String headerText = frameModel.getInvocationExpression();
			header.setText(headerText);

			PandionJLayoutAlgorithm layoutAlg = new PandionJLayoutAlgorithm();
			layoutAlg.addObserver(viewSizeObserver);
			viewer.setLayoutAlgorithm(layoutAlg);

			updateLook();

			setExpanded(true);
			slider.setMinimum(1);
			slider.setMaximum(1);

			viewer.setInput(frameModel);
			viewer.setLabelProvider(new FigureProvider(frameModel));
			viewer.applyLayout();
		}
	}

	private Observer viewSizeObserver = new Observer() {
		private Point lastSize;

		public void update(Observable o, Object arg) {
			PandionJUI.executeUpdate(() -> {
				Point size = viewer.getGraphControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
				if(!size.equals(lastSize)) {
					lastSize = size;
					viewer.getGraphControl().setLayoutData(new GridData(getBounds().width - 20, size.y + Constants.MARGIN));
					viewer.getGraphControl().requestLayout();
					viewer.getGraphControl().scrollToY(size.y);
				}
			});
		}
	};

	private Observer runtimeObserver = new Observer() {
		public void update(Observable o, Object arg) {
//			updateLook();
			//			RuntimeModel runtime = (RuntimeModel) o;
			//			if(runtime.isTerminated())
			//				runtime.unregisterObserver(runtimeObserver);
			//			else {
//			viewer.refresh();
//			viewer.applyLayout();
			//			}
		}
	};

	private void updateLook() {
		if(model.isObsolete()) {
			setBackground(Constants.Colors.OBSOLETE);
			//			setExpanded(false);
		}
		else if(model.isExecutionFrame()) {
			setBackground(Constants.Colors.INST_POINTER);
			//			setExpanded(true);
		}
		else
			setBackground(null);
	}

	private Observer stackObserver = new Observer() {
		public void update(Observable o, Object list) {
			header.setText(model.getInvocationExpression());
			if(model.isObsolete()) {
				model.unregisterObserver(stackObserver);
				model.getRuntime().unregisterObserver(runtimeObserver);
			}
			else {
				@SuppressWarnings("unchecked")
				List<VariableModel<?>> newVars = (List<VariableModel<?>>) list;
				if(!newVars.isEmpty()) { 
					viewer.refresh();
					viewer.applyLayout();
				}
				if(model.exceptionOccurred())
					setError(model.getExceptionMessage());

				slider.setMaximum(model.getRunningStep()+1);
				slider.setSelection(model.getStepPointer()+1);
				slider.setToolTipText(slider.getSelection() + "/" + slider.getMaximum());
			}
		}
	};





	void copyToClipBoard() {
		Graph item = viewer.getGraphControl();
		Point p = item.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		Rectangle size = item.getClientArea();

		System.out.println(p +  "    " + size);
		//			compositeViewer.setBackground(Constants.HIGHLIGHT_COLOR);
		GC gc = new GC(item);
		//			Rectangle clipping2 = gc.getClipping();
		//			Image img = new Image(Display.getDefault(), size.width, size.height);
		//			gc.copyArea(img, 0, 0);
		//			ImageData imageData = img.getImageData();

		RGB[] rgb = new RGB[256];
		// build grey scale palette: 256 different grey values are generated. 
		for (int i = 0; i < 256; i++) {
			rgb[i] = new RGB(i, i, i);
		}

		// Construct a new indexed palette given an array of RGB values.
		PaletteData palette = new PaletteData(rgb);
		Image img2 = new Image(Display.getDefault(), new ImageData(size.width, size.height, 8, palette));
		//			gc.setClipping(0, 0, p.x, p.y);
		gc.copyArea(img2, 0, 0);
		Shell popup = new Shell(Display.getDefault());
		popup.setText("Image");
		popup.setBounds(50, 50, 200, 200);
		Canvas canvas = new Canvas(popup, SWT.NONE);
		canvas.setBounds(img2.getBounds());
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				e.gc.drawImage(img2, 0, 0);
			}
		});
		popup.open();
		Clipboard clipboard = new Clipboard(Display.getDefault());
		clipboard.setContents(new Object[]{img2.getImageData()}, new Transfer[]{ ImageTransfer.getInstance()}); 
		img2.dispose();
		gc.dispose();
	}


}