package pt.iscte.pandionj;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObservableModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.figures.ArrayPrimitiveFigure2;
import pt.iscte.pandionj.figures.ArrayReferenceFigure;
import pt.iscte.pandionj.figures.NullFigure;
import pt.iscte.pandionj.figures.ObjectFigure;
import pt.iscte.pandionj.figures.PandionJFigure;
import pt.iscte.pandionj.figures.ReferenceFigure;
import pt.iscte.pandionj.figures.ValueFigure;

public class FigureProvider2  {
	private Map<IObservableModel, PandionJFigure<?>> figCache = new WeakHashMap<>();

	private IStackFrameModel stackFrame;

	public FigureProvider2(IStackFrameModel stackFrame) {
		this.stackFrame = stackFrame;
	}

	public PandionJFigure<?> getFigure(Object element) {
		PandionJFigure<?> fig = figCache.get(element);
		if(fig == null) {
			IObservableModel model = (IObservableModel) element;
			//			fig = model.createFigure();
			fig = createFigure(model);
			figCache.put(model, fig);
		}
		return fig;		
	}

	private PandionJFigure<?> createFigure(IObservableModel model) {
		PandionJFigure<?> innerFig = null;
		
		if(model instanceof IValueModel) {
			innerFig = new ValueFigure((IValueModel) model);
		}
		else if(model instanceof IReferenceModel) {
			innerFig = new ReferenceFigure((IReferenceModel) model);
		}
		else if(model instanceof IEntityModel) {
			IEntityModel entity = (IEntityModel) model;
			if(entity.isNull())
				innerFig = new NullFigure(entity);
			else {
				Set<String> tags = getEntityTags(entity);

				if(model instanceof IArrayModel) {
					IArrayModel aModel = (IArrayModel) model;
					IArrayWidgetExtension arrayExtension = ExtensionManager.getArrayExtension(aModel, tags);
//					innerFig = arrayExtension.createFigure(aModel); // TODO repor
					if(innerFig == null) {
						if(aModel.isPrimitive()) {
							innerFig = new ArrayPrimitiveFigure2(aModel);
						}
						else {
							innerFig = new ArrayReferenceFigure(aModel);
						}
					}
				}
				else if(model instanceof IObjectModel) {
					IObjectModel oModel = (IObjectModel) model; 
					IFigure extensionFigure = ExtensionManager.getObjectExtension(oModel).createFigure(oModel);
					innerFig = new ObjectFigure(oModel, extensionFigure, true);
				}
			}
		}
		assert innerFig != null : model;
//		BaseFigure base = new BaseFigure(innerFig);
		return innerFig;
	}


	private Set<String> getEntityTags(IEntityModel e) {
		Set<String> tags = new HashSet<String>();
		for(IReferenceModel r : stackFrame.getReferencesTo(e))
			tags.addAll(r.getTags());
		return tags;
	}

	//	protected IFigure createExtensionFigure(ObjectModel model) {
	////		if(model.hasAttributeTags()) {
	////			extension = ExtensionManager.createTagExtension(model);
	////		}
	////		else if(extension == null) {
	//			extension = ExtensionManager.getObjectExtension(model).createFigure(model);
	////		}
	//		
	//		return extension.createFigure(this);
	//	}


//	private void selfStyleConnection(Object element, GraphConnection connection) {
//		PolylineConnection conn = (PolylineConnection) connection.getConnectionFigure();
//		Pointer pointer = (Pointer) element;
//
//		if(pointer.isNull()) {
//			PolygonDecoration decoration = new PolygonDecoration();
//			PointList points = new PointList();
//			points.addPoint(0,-1); // 1
//			points.addPoint(0, 1); // -1
//			decoration.setTemplate(points);
//			decoration.setScale(Constants.ARROW_EDGE, Constants.ARROW_EDGE);
//			decoration.setLineWidth(Constants.ARROW_LINE_WIDTH);
//			decoration.setOpaque(true);
//			conn.setTargetDecoration(decoration);	
//		}
//		else {
//			PolylineDecoration decoration = new PolylineDecoration();
//			PointList points = new PointList();
//			points.addPoint(-1, -1);
//			points.addPoint(0, 0);
//			points.addPoint(-1, 1);
//			decoration.setTemplate(points);
//			decoration.setScale(Constants.ARROW_EDGE, Constants.ARROW_EDGE);
//			decoration.setLineWidth(Constants.ARROW_LINE_WIDTH);
//			decoration.setOpaque(true);
//			conn.setTargetDecoration(decoration);
//
//			//			fig.setSourceAnchor(new PositionAnchor(connection.getSource().getNodeFigure(), Position.RIGHT));
//			//			fig.setTargetAnchor(new PositionAnchor(connection.getDestination().getNodeFigure(), Position.LEFT));
//		}
//
//		IFigure sFig = ((BaseFigure) connection.getSource().getNodeFigure()).getInnerFigure();
//		IFigure tFig = ((BaseFigure) connection.getDestination().getNodeFigure()).getInnerFigure();
//
//		if(sFig instanceof ArrayReferenceFigure) {
//			String refName = ((Pointer) element).reference.getName();
//			refName = refName.substring(1, refName.length()-1);
//			conn.setSourceAnchor(((ArrayReferenceFigure) sFig).getAnchor(Integer.parseInt(refName)));
//		}
//
//		if(tFig instanceof ArrayPrimitiveFigure2 || tFig instanceof ArrayReferenceFigure) {
//			conn.setTargetAnchor(new PositionAnchor(tFig, Position.TOPLEFT));
//		}
//
//
//
////		handleIllustration(connection.getSource(), sFig, connection.getDestination(), tFig, pointer.reference);
//
//		// TODO repor
//		//		fig.setVisible(pointer.source.isWithinScope() && pointer.target.isWithinScope());
//	}

//	public void handleIllustration(IReferenceModel reference, IFigure sourceFig, IFigure targetFig) {
//		
//			if(targetFig instanceof ArrayPrimitiveFigure2 &&  reference.hasIndexVars()) {
//				IllustrationBorder b = new IllustrationBorder(reference, (ArrayPrimitiveFigure2) targetFig);
//				targetFig.setBorder(b);
//			}
		
//		else if(source.getData() instanceof ArrayReferenceModel && 
//				target.getData() instanceof ArrayPrimitiveModel) {
//
//			ArrayReferenceModel array = (ArrayReferenceModel) source.getData();
//			targetFig.setBorder(new IllustrationBorder(reference, (ArrayPrimitiveFigure2) targetFig));
//		}
//
//	}
//	} 

	enum Position {
		TOP {
			@Override
			Point getPoint(Rectangle r) {
				return new Point(r.x + r.width/2, r.y);
			}
		},
		RIGHT {
			@Override
			Point getPoint(Rectangle r) {
				return new Point(r.x + r.width, r.y + r.height/2);
			}
		},
		BOTTOM {
			@Override
			Point getPoint(Rectangle r) {
				return new Point(r.x + r.width/2, r.y + r.height);
			}
		},
		LEFT {
			@Override
			Point getPoint(Rectangle r) {
				return new Point(r.x, r.y + r.height/2);
			}
		},
		TOPLEFT {
			@Override
			Point getPoint(Rectangle r) {
				return new Point(r.x, r.y + r.height/4);
			}
		};

		abstract Point getPoint(org.eclipse.draw2d.geometry.Rectangle r);
	}

	private class PositionAnchor extends AbstractConnectionAnchor {

		private Position position;

		public PositionAnchor(IFigure fig, Position position) {
			super(fig);
			this.position = position;
		}

		@Override
		public Point getLocation(Point reference) {
			org.eclipse.draw2d.geometry.Rectangle r =  org.eclipse.draw2d.geometry.Rectangle.SINGLETON;
			r.setBounds(getOwner().getBounds());
			r.translate(0, 0);
			r.resize(1, 1);
			getOwner().translateToAbsolute(r);
			return position.getPoint(r);
		}
	}
}
