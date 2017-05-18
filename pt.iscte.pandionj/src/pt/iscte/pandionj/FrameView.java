package pt.iscte.pandionj;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
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
import org.eclipse.ui.ide.IDE;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.ZestStyles;

import pt.iscte.pandionj.model.StackFrameModel;

class FrameView extends Composite {
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
			compositeHeader.setLayout(new FillLayout());

			// TODO image manager
//			new Label(compositeHeader,SWT.NONE).setImage(image("frame.gif"));
			header = new Label(compositeHeader, SWT.BORDER);
			
			FontManager.setFont(header, Constants.MESSAGE_FONT_SIZE);

			//			Slider slider = new Slider(compositeHeader, SWT.HORIZONTAL | SWT.BORDER);
			//			slider.setMaximum(1);
			//			slider.setMaximum(4);

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
		}


		public void setExpanded(boolean expanded) {
			setLayoutData(new GridData(SWT.FILL, expanded ? SWT.FILL : SWT.DEFAULT, true, expanded));
			gridData.exclude = !expanded;
			compositeViewer.setVisible(expanded);
			getParent().layout();
			if(expanded)
				navigateToLine(model.getSourceFile(), model.getLineNumber());
		}

		public boolean isExpanded() {
			return !gridData.exclude;
		}

		public void setZoom(double zoom) {
			viewer.setZoom(zoom);
		}

		private void setError(String message) {
			setBackground(Constants.ERROR_COLOR);
			header.setToolTipText(message);
		}

		void setInput(StackFrameModel frameModel) {
			Color c = frameModel.isObsolete() ? ColorManager.getColor(150, 150, 150) : null;
			setBackground(c);
//			header.setBackground(c);
			
			header.setToolTipText("");
			viewer.getGraphControl().setToolTipText(frameModel.getSourceFile().getName() + " on line " + frameModel.getLineNumber());
			if(this.model != frameModel) {
				this.model = frameModel;
				String headerText = frameModel.getInvocationExpression();
				header.setText(headerText);
				compositeHeader.pack();
				viewer.setInput(frameModel);
				viewer.setLayoutAlgorithm(new PandionJLayoutAlgorithm());
				viewer.getGraphControl().setEnabled(true);
				if(frameModel.isObsolete())
					setBackground(ColorManager.getColor(150, 150, 150));
				
				setExpanded(model.isExecutionFrame() || model.exceptionOccurred() || model.getCallStack().isTerminated());
				
				frameModel.registerDisplayObserver(new Observer() {
					public void update(Observable o, Object e) {
						header.setText(frameModel.getInvocationExpression());
						compositeHeader.pack();
						viewer.refresh(); // TODO dupla chamada?
						viewer.applyLayout();
						if(model.exceptionOccurred())
							setError(model.getExceptionMessage());
						else if(frameModel.isObsolete())
							setBackground(ColorManager.getColor(150, 150, 150));
						
						setExpanded(model.isExecutionFrame() || model.exceptionOccurred() || model.getCallStack().isTerminated());
					}
				}, viewer.getControl());
				getParent().layout();
			}
		}

		
		
		
		
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
		
		private static void navigateToLine(IFile file, Integer line) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(IMarker.LINE_NUMBER, line);
			IMarker marker = null;
			try {
				marker = file.createMarker(IMarker.TEXT);
				marker.setAttributes(map);
				try {
					IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), marker);
				} catch ( PartInitException e ) {
					//complain
				}
			} catch ( CoreException e1 ) {
				//complain
			} finally {
				try {
					if (marker != null)
						marker.delete();
				} catch ( CoreException e ) {
					//whatever
				}
			}
		}
	}