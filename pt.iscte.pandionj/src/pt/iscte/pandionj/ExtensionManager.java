package pt.iscte.pandionj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jdt.core.IType;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectWidgetExtension;
import pt.iscte.pandionj.extensions.ColorAguia;
import pt.iscte.pandionj.extensions.ColorWidget;
import pt.iscte.pandionj.extensions.GrayscaleImageWidget;
import pt.iscte.pandionj.extensions.HistogramWidget;
import pt.iscte.pandionj.extensions.ImageAguia;
import pt.iscte.pandionj.extensions.IterableWidget;
import pt.iscte.pandionj.extensions.NumberWidget;
import pt.iscte.pandionj.extensions.StringWidget;
import pt.iscte.pandionj.model.ModelObserver;

public class ExtensionManager {

	private static Map<String, IArrayWidgetExtension> arrayExtensions;
	private static List<IObjectWidgetExtension> objectExtensions;

	static {
		arrayExtensions = new HashMap<String, IArrayWidgetExtension>();
		arrayExtensions.put("image", new GrayscaleImageWidget());
		arrayExtensions.put("hist", new HistogramWidget());

		objectExtensions = new ArrayList<>();
		objectExtensions.add(new NumberWidget());
		objectExtensions.add(new StringWidget());
		objectExtensions.add(new ColorWidget());
		objectExtensions.add(new IterableWidget());
		objectExtensions.add(new ColorAguia());
		objectExtensions.add(new ImageAguia());
	}


	// TODO composite extension? (as TagExtension?)
	public static IArrayWidgetExtension getArrayExtension(IArrayModel<?> m, Set<String> tags) {

		for (String tag : tags) {
			IArrayWidgetExtension ext = arrayExtensions.get(tag);
			if(ext != null && ext.accept(m))
				return ext;
		}
		return IArrayWidgetExtension.NULL_EXTENSION;
	}


	public static IObjectWidgetExtension getObjectExtension(IObjectModel m) {
		if(m.hasAttributeTags())
			return new TagExtension(m.getAttributeTags());

		IType type = m.getType();
		for(IObjectWidgetExtension ext : ExtensionManager.objectExtensions)
			if(ext.accept(type))
				return ext;
		return IObjectWidgetExtension.NULL_EXTENSION;
	}


	//	public static IObjectWidgetExtension createTagExtension(ObjectModel m) {
	//		return new TagExtension(m.getAttributeTags());
	//	}

	static class TagExtension implements IObjectWidgetExtension {

		private Multimap<String, String> tags;
		private Multimap<String, IFigure> figs;
		private IFigure compositeFig;

		public TagExtension(Multimap<String, String> tags) {
			assert tags != null;
			this.tags = tags;
			figs = ArrayListMultimap.create();
		}

		@Override
		public IFigure createFigure(IObjectModel m) {
			// TODO observe ref change
			m.registerDisplayObserver(new ModelObserver() {
				@Override
				public void update(Object arg) {
					String attName = (String) arg;
					if(figs.containsKey(attName)) {
						for (IFigure f : figs.get(attName))
							compositeFig.remove(f);

						figs.removeAll(attName);
						addChildFigures(m, attName);
					}
				}
			});

			compositeFig = new Figure();
			compositeFig.setLayoutManager(new FlowLayout());
			for (Entry<String, Collection<String>> e : tags.asMap().entrySet())
				addChildFigures(m, e.getKey());

			compositeFig.setBackgroundColor(ColorConstants.blue);
			return compositeFig;
		}

		// FIXME field values on null / undefined fields
		private void addChildFigures(IObjectModel m, String attName) {
			for (String tag : tags.get(attName)) {
				IArrayWidgetExtension ext = arrayExtensions.get(tag);
				if(ext != null) {
					IArrayModel array = m.getArray(attName);
					if(array != null) {
						IFigure f = ext.createFigure(array);
						f.setToolTip(new Label(attName));
						figs.put(attName, f);
						compositeFig.add(f);
					}
				}
			}
		}

		@Override
		public boolean accept(IType objectType) {
			return true;
		}

	}

	public static Set<String> validTags() {
		return Collections.unmodifiableSet(arrayExtensions.keySet());
	}
}
