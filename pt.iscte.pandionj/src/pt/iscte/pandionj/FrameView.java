package pt.iscte.pandionj;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;

import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.model.StackFrameModel;

class FrameView extends Composite {
	FrameViewer viewer;
	StackFrameModel model;
	Label header;
	Slider slider;
	
	FrameView(Composite parent) {
		super(parent, SWT.BORDER);
		GridLayout layout = new GridLayout();
		setLayout(layout);
		setBackground(ColorConstants.white);
		header = new Label(this, SWT.BORDER);
		header.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		FontManager.setFont(header, Constants.MESSAGE_FONT_SIZE);

		viewer = new FrameViewer(this);

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
			String headerText = frameModel.getInvocationExpression();
			header.setText(headerText);

			viewer.setModel(frameModel,(v) -> !v.isStatic());
			slider.setMinimum(1);
			slider.setMaximum(1);
		}
		updateLook();
	}

	


	private void updateLook() {
		if(model.isObsolete())
			setObsolete();
		else if(model.isExecutionFrame())
			setBackground(Constants.Colors.INST_POINTER);
		else
			setBackground(null);
	}
	
	public void setObsolete() {
		setBackground(Constants.Colors.OBSOLETE);
	}


//	private Observer stackObserver = new Observer() {
//		public void update(Observable o, Object list) {
//			header.setText(model.getInvocationExpression());
//			if(model.isObsolete()) {
//				model.unregisterObserver(stackObserver);
//			}
//			else {
//				@SuppressWarnings("unchecked")
//				List<VariableModel<?>> newVars = (List<VariableModel<?>>) list;
//				if(!newVars.isEmpty()) { 
//					viewer.refresh();
//					viewer.applyLayout();
//				}
//				if(model.exceptionOccurred())
//					setError(model.getExceptionMessage());
//
//				slider.setMaximum(model.getRunningStep()+1);
//				slider.setSelection(model.getStepPointer()+1);
//				slider.setToolTipText(slider.getSelection() + "/" + slider.getMaximum());
//			}
//		}
//	};


	
	
	
	
	



	void copyToClipBoard() {
		Composite item = viewer;
		Point p = viewer.computeSize(SWT.DEFAULT, SWT.DEFAULT);

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