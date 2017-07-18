package pt.iscte.pandionj;


import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.zest.core.viewers.IConnectionStyleProvider;
import org.eclipse.zest.core.viewers.IFigureProvider;
import org.eclipse.zest.core.viewers.ISelfStyleProvider;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;

import pt.iscte.pandionj.NodeProvider.Pointer;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.figures.ArrayPrimitiveFigure;
import pt.iscte.pandionj.figures.ArrayReferenceFigure;
import pt.iscte.pandionj.figures.BaseFigure;
import pt.iscte.pandionj.figures.NullFigure;
import pt.iscte.pandionj.figures.ObjectFigure;
import pt.iscte.pandionj.figures.ReferenceFigure;
import pt.iscte.pandionj.figures.ValueFigure;
import pt.iscte.pandionj.model.ArrayModel;
import pt.iscte.pandionj.model.ArrayPrimitiveModel;
import pt.iscte.pandionj.model.ArrayReferenceModel;
import pt.iscte.pandionj.model.EntityModel;
import pt.iscte.pandionj.model.ModelElement;
import pt.iscte.pandionj.model.NullModel;
import pt.iscte.pandionj.model.ObjectModel;
import pt.iscte.pandionj.model.ReferenceModel;
import pt.iscte.pandionj.model.StackFrameModel;
import pt.iscte.pandionj.model.ValueModel;

class FigureProvider extends LabelProvider implements IFigureProvider, IConnectionStyleProvider, ISelfStyleProvider {
	private Map<ModelElement<?>, IFigure> figCache = new WeakHashMap<>();

	private StackFrameModel stackFrame;

	public FigureProvider(StackFrameModel stackFrame) {
		this.stackFrame = stackFrame;
	}

	@Override
	public IFigure getFigure(Object element) {
		IFigure fig = figCache.get(element);
		if(fig == null) {
			ModelElement<?> model = (ModelElement<?>) element;
			//			fig = model.createFigure();
			fig = createFigure(model);
			figCache.put(model, fig);
		}
		return fig;		
	}

	private IFigure createFigure(ModelElement<?> model) {
		IFigure innerFig = null;

		if(model instanceof ValueModel) {
			ValueModel vModel = (ValueModel) model;
			innerFig = new ValueFigure(vModel, vModel.getRole());
		}
		else if(model instanceof ReferenceModel) {
			ReferenceModel rModel = (ReferenceModel) model;
			innerFig = new ReferenceFigure(rModel);
		}
		else if(model instanceof NullModel) {
			innerFig = new NullFigure();
		}
		else { // entities
			Set<String> tags = getEntityTags((EntityModel<?>) model);

			if(model instanceof ArrayModel) {
				ArrayModel aModel = (ArrayModel) model;
				IArrayWidgetExtension arrayExtension = ExtensionManager.getArrayExtension(aModel, tags);
				innerFig = arrayExtension.createFigure(aModel);
				if(innerFig == null) {
					if(model instanceof ArrayPrimitiveModel) {
						innerFig = new ArrayPrimitiveFigure((ArrayPrimitiveModel) model);
					}
					else if(model instanceof ArrayReferenceModel) {
						innerFig = new ArrayReferenceFigure((ArrayReferenceModel) model);
					}
				}
			}
			else if(model instanceof ObjectModel) {
				ObjectModel oModel = (ObjectModel) model; 
				IFigure extensionFigure = ExtensionManager.getObjectExtension(oModel).createFigure(oModel);
				innerFig = new ObjectFigure(oModel, extensionFigure, true);
			}
		}
		assert innerFig != null;
		BaseFigure base = new BaseFigure(innerFig);
		return base;
	}


	private Set<String> getEntityTags(EntityModel<?> e) {
		Set<String> tags = new HashSet<String>();
		Collection<ReferenceModel> references = stackFrame.getReferencesTo(e);
		for(ReferenceModel r : references)
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
	
	

	@Override
	public String getText(Object e) {
		//		if(element instanceof EntityConnectionData)
		//			return "";
		//		else if(element instanceof Pointer)
		//			return ((Pointer) element).refName;
		//		else
		//			return element.toString();
		//		if(e instanceof Pointer && !((Pointer) e).isTopLevel())
		return null;
	}

	@Override
	public IFigure getTooltip(Object entity) {
		if(entity instanceof Pointer)
			return new Label(((Pointer) entity).refName);
		else
			return new Label(entity.toString());
	}

	@Override
	public int getConnectionStyle(Object rel) {
		return ZestStyles.CONNECTIONS_SOLID;
	}

	@Override
	public Color getColor(Object rel) {
		return ColorConstants.black;
	}

	@Override
	public Color getHighlightColor(Object rel) {
		return Constants.Colors.SELECT;
	}

	@Override
	public int getLineWidth(Object rel) {
		return Constants.ARROW_LINE_WIDTH;
	}

	@Override
	public void selfStyleNode(Object element, GraphNode node) {
		node.getNodeFigure().setSize(-1, -1);
	}

	@Override
	public void selfStyleConnection(Object element, GraphConnection connection) {
		PolylineConnection fig = (PolylineConnection) connection.getConnectionFigure();
		Pointer pointer = (Pointer) element;

		if(pointer.isNull()) {
			PolygonDecoration decoration = new PolygonDecoration();
			PointList points = new PointList();
			points.addPoint(0,-1); // 1
			points.addPoint(0, 1); // -1
			decoration.setTemplate(points);
			decoration.setScale(Constants.ARROW_EDGE, Constants.ARROW_EDGE);
			decoration.setLineWidth(Constants.ARROW_LINE_WIDTH);
			decoration.setOpaque(true);
			fig.setTargetDecoration(decoration);	
		}
		else {
			PolylineDecoration decoration = new PolylineDecoration();
			PointList points = new PointList();
			points.addPoint(-1, -1);
			points.addPoint(0, 0);
			points.addPoint(-1, 1);
			decoration.setTemplate(points);
			decoration.setScale(Constants.ARROW_EDGE, Constants.ARROW_EDGE);
			decoration.setLineWidth(Constants.ARROW_LINE_WIDTH);
			decoration.setOpaque(true);
			fig.setTargetDecoration(decoration);

			//			fig.setSourceAnchor(new PositionAnchor(connection.getSource().getNodeFigure(), Position.RIGHT));
			//			fig.setTargetAnchor(new PositionAnchor(connection.getDestination().getNodeFigure(), Position.LEFT));
		}

		IFigure sFig = ((BaseFigure) connection.getSource().getNodeFigure()).innerFig;

		if(sFig instanceof ArrayReferenceFigure) {
			// TODO anchor
			//			String refName = ((Pointer) element).refName;
			//			refName = refName.substring(1, refName.length()-1);
			//			fig.setSourceAnchor(((ArrayReferenceFigure) sFig).getAnchor(Integer.parseInt(refName)));
		}
		else if(sFig instanceof ArrayPrimitiveFigure) {
			fig.setTargetAnchor(new PositionAnchor(connection.getDestination().getNodeFigure(), Position.LEFT));
		}

		// TODO repor
		//		fig.setVisible(pointer.source.isWithinScope() && pointer.target.isWithinScope());
	}

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
