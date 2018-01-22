package pt.iscte.pandionj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
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
import pt.iscte.pandionj.extensibility.ITypeWidgetExtension;
import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.extensibility.IValueWidgetExtension;
import pt.iscte.pandionj.extensibility.ModelObserver;
import pt.iscte.pandionj.extensibility.PandionJConstants;

public class ExtensionManager {

	private static Map<String, IArrayWidgetExtension> arrayExtensions;
	private static Map<String, String> arrayExtensionDescriptions;

	private static Map<String, IValueWidgetExtension> valueExtensions;
	private static Map<String, String> valueExtensionDescriptions;
	
	private static List<ITypeWidgetExtension> objectExtensions;

	static {
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();

		arrayExtensions = new HashMap<String, IArrayWidgetExtension>();
		arrayExtensionDescriptions = new HashMap<String, String>();
		
		IConfigurationElement[] extsArray = extensionRegistry.getConfigurationElementsFor(PandionJConstants.ARRAYTAG_EXTENSION_ID);
		for(IConfigurationElement e : extsArray) {
			try {
				String name = e.getAttribute("name");
				IArrayWidgetExtension ae = (IArrayWidgetExtension) e.createExecutableExtension("class");
				String tag = "@" + name;
				arrayExtensions.put(tag, ae);
				arrayExtensionDescriptions.put(tag, e.getAttribute("where"));
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
		}

		valueExtensions = new HashMap<>();
		valueExtensionDescriptions = new HashMap<String, String>();
		
		IConfigurationElement[] extsValue = extensionRegistry.getConfigurationElementsFor(PandionJConstants.VALUETAG_EXTENSION_ID);
		for(IConfigurationElement e : extsValue) {
			try {
				String name = e.getAttribute("name");
				IValueWidgetExtension ae = (IValueWidgetExtension) e.createExecutableExtension("class");
				String tag = "@" + name;
				valueExtensions.put(tag, ae);
				valueExtensionDescriptions.put(tag, e.getAttribute("where"));

			} catch (CoreException e1) {
				e1.printStackTrace();
			}
		}

		objectExtensions = new ArrayList<>();
		IConfigurationElement[] extsObj = extensionRegistry.getConfigurationElementsFor(PandionJConstants.TYPE_EXTENSION_ID);
		for(IConfigurationElement e : extsObj) {
			try {
				ITypeWidgetExtension tw = (ITypeWidgetExtension) e.createExecutableExtension("class");
				objectExtensions.add(tw);
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
		}		
	}

	public static IArrayWidgetExtension getArrayExtension(IArrayModel<?> m, Collection<String> tags) {
		for (String tag : tags) {
			IArrayWidgetExtension ext = arrayExtensions.get(tag);
			if(ext != null && ext.accept(m))
				return ext;
		}
		return IArrayWidgetExtension.NULL_EXTENSION;
	}


	public static IValueWidgetExtension getValueExtension(IValueModel v, Collection<String> tags) {
		for (String tag : tags) {
			IValueWidgetExtension ext = valueExtensions.get(tag);
			if(ext != null && ext.accept(v))
				return ext;
		}
		
		return IValueWidgetExtension.NULL_EXTENSION;
	}
	
	public static ITypeWidgetExtension getObjectExtension(IObjectModel m) {
		if(m.hasAttributeTags())
			return new TagExtension(m.getAttributeTags());

		IType type = m.getType();
		if(type != null) {
			for(ITypeWidgetExtension ext : ExtensionManager.objectExtensions)
				if(ext.accept(type))
					return ext;
		}
		return ITypeWidgetExtension.NULL_EXTENSION;
	}


	public static Set<String> validTags() {
		Set<String> validTags = new HashSet<String>();
		validTags.addAll(arrayExtensions.keySet());
		validTags.addAll(valueExtensions.keySet());
		return validTags;
	}
	
	static class TagDescription {
		final String tag;
		String description;

		TagDescription(String tag, String description) {
			this.tag = tag;
			this.description = description;
		}
	}
	
	public static List<TagDescription> getTagDescriptions() {
		List<TagDescription> list = new ArrayList<>();
		for(Entry<String, String> e : arrayExtensionDescriptions.entrySet())
			list.add(new TagDescription(e.getKey(), e.getValue()));
		
		for(Entry<String, String> e : valueExtensionDescriptions.entrySet()) {
			TagDescription existing = null;
			for(TagDescription t : list)
				if(t.tag.equals(e.getKey())) {
					existing = t;
					break;
				}
			
			if(existing == null)
				list.add(new TagDescription(e.getKey(), e.getValue()));
			else
				existing.description += "\n" + e.getValue();
		}
		return list;
	}
	
	static class TagExtension implements ITypeWidgetExtension {

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
			m.registerDisplayObserver(new ModelObserver<Object>() {
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

			return compositeFig;
		}

		private void addChildFigures(IObjectModel m, String attName) {
			IArrayModel<?> array = m.getArray(attName);
			if(array != null) {
				IArrayWidgetExtension ext = getArrayExtension(array, tags.get(attName));
				if(ext != IArrayWidgetExtension.NULL_EXTENSION) {
					IFigure f = ext.createFigure(array);
					f.setToolTip(new Label(attName));
					figs.put(attName, f);
					compositeFig.add(f);
				}
			}
		}

		@Override
		public boolean accept(IType objectType) {
			return true;
		}

	}

	
}
