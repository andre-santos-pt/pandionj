package pt.iscte.pandionj.perspective;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;


/**
 *  This class is meant to serve as an example for how various contributions 
 *  are made to a perspective. Note that some of the extension point id's are
 *  referred to as API constants while others are hardcoded and may be subject 
 *  to change. 
 */
public class IsctePerspective implements IPerspectiveFactory {

	private IPageLayout factory;

	public IsctePerspective() {
		super();
	}
	
	private void setupPrefs() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode("org.eclipse.debug.ui");
		prefs.put("org.eclipse.debug.ui.activate_debug_view", "false");
		prefs.put("org.eclipse.debug.ui.switch_perspective_on_suspend", "never");
		IEclipsePreferences prefsUi = InstanceScope.INSTANCE.getNode("org.eclipse.ui.ide");
		prefsUi.put("SWITCH_PERSPECTIVE_ON_PROJECT_CREATION", "never");
	
		IEclipsePreferences prefsJdt = InstanceScope.INSTANCE.getNode("org.eclipse.jdt.ui");
		prefsJdt.put("hoverModifiers", " org.eclipse.jdt.ui.BestMatchHover;!0;org.eclipse.jdt.internal.debug.ui.JavaDebugHover;!0;org.eclipse.jdt.ui.ProblemHover;!0;org.eclipse.jdt.ui.NLSStringHover;Command+Alt;org.eclipse.jdt.ui.JavadocHover;Command+Shift;org.eclipse.jdt.ui.AnnotationHover;!0;org.eclipse.jdt.ui.JavaSourceHover;Shift;");
//		                                org.eclipse.jdt.ui.BestMatchHover;!0;org.eclipse.jdt.internal.debug.ui.JavaDebugHover;!0;org.eclipse.jdt.ui.ProblemHover;!0;org.eclipse.jdt.ui.NLSStringHover;Command+Alt;org.eclipse.jdt.ui.JavadocHover;Command+Shift;org.eclipse.jdt.ui.AnnotationHover;!0;org.eclipse.jdt.ui.JavaSourceHover;Shift;

		char c = '\u0000';
		prefsJdt.put("content_assist_disabled_computers", 
				"org.eclipse.jdt.ui.swtProposalCategory" + c + 
				"org.eclipse.jdt.ui.javaNoTypeProposalCategory" + c + 
				"org.eclipse.jdt.ui.javaTypeProposalCategory" + c + 
				"org.eclipse.jdt.ui.textProposalCategory" + c +  
				"org.eclipse.jdt.ui.javaAllProposalCategory" + c + 
				"org.eclipse.e4.tools.jdt.templates.e4ProposalCategory" + c + 
				"org.eclipse.pde.api.tools.ui.apitools_proposal_category" + c + 
				"org.eclipse.recommenders.calls.rcp.proposalCategory.templates" + c + 
				"org.eclipse.recommenders.chain.rcp.proposalCategory.chain" + c + 
				"org.eclipse.recommenders.completion.rcp.proposalCategory.intelligent" + c + 
				"org.eclipse.jdt.ui.templateProposalCategory" + c);
		
		prefsJdt.put("closeBraces", "false");
		prefsJdt.put("closeBrackets", "false");
		prefsJdt.put("closeJavaDocs", "false");
		prefsJdt.put("closeStrings", "false");
		prefsJdt.put("smart_opening_brace", "false");
		prefsJdt.put("content_assist_fill_method_arguments", "false");
		prefsJdt.put("editor_folding_enabled", "false");

		IEclipsePreferences prefsWb = InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench");
		prefsWb.put("org.eclipse.debug.ui.consoleFont","1|Monaco|14.0|0|COCOA|1|Monaco");

		
	}

	public void createInitialLayout(IPageLayout factory) {
		setupPrefs();
		this.factory = factory;
		factory.setEditorAreaVisible(true);
		factory.setFixed(true);
		
		addViews();
		factory.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.JavaProjectWizard");
		factory.addNewWizardShortcut("pt.iscte.perspective.wizards.NewFileWizard");
//		addActionSets();
//		addNewWizardShortcuts();
//		addPerspectiveShortcuts();
//		addViewShortcuts();
	}

	private void addViews() {
		// Creates the overall folder layout. 
		// Note that each new Folder uses a percentage of the remaining EditorArea.
		
//		IFolderLayout bottom = factory.createFolder("bottomRight", IPageLayout.BOTTOM, 0.8f, factory.getEditorArea());
//		bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
//		bottom.addView("org.eclipse.team.ui.GenericHistoryView"); //NON-NLS-1
//		bottom.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
//		bottom.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		
//		IFolderLayout topLeft = factory.createFolder("topLeft", IPageLayout.LEFT, 0.2f, factory.getEditorArea());
//		topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);
//		topLeft.setProperty(id, value);
//		topLeft.addView("org.eclipse.jdt.junit.ResultView"); //NON-NLS-1

//		IFolderLayout topRight = factory.createFolder("topRight", IPageLayout.RIGHT, 0.5f, factory.getEditorArea());
//		topRight.addView("pt.iscte.pandionj.view");
		factory.addStandaloneView("org.eclipse.jdt.ui.PackageExplorer", false, IPageLayout.LEFT, 0.15f, factory.getEditorArea());
		
		factory.addStandaloneView("pt.iscte.pandionj.view", false, IPageLayout.RIGHT, 0.5f, factory.getEditorArea());
		
		factory.addStandaloneView(IConsoleConstants.ID_CONSOLE_VIEW, false, IPageLayout.BOTTOM, 0.8f, "pt.iscte.pandionj.view");
		
//		factory.addFastView("org.eclipse.team.ccvs.ui.RepositoriesView",0.50f); //NON-NLS-1
//		factory.addFastView("org.eclipse.team.sync.views.SynchronizeView", 0.50f); //NON-NLS-1
	}

	private void addActionSets() {
		factory.addActionSet("org.eclipse.debug.ui.launchActionSet"); //NON-NLS-1
		factory.addActionSet("org.eclipse.debug.ui.debugActionSet"); //NON-NLS-1
		factory.addActionSet("org.eclipse.debug.ui.profileActionSet"); //NON-NLS-1
		factory.addActionSet("org.eclipse.jdt.debug.ui.JDTDebugActionSet"); //NON-NLS-1
		factory.addActionSet("org.eclipse.jdt.junit.JUnitActionSet"); //NON-NLS-1
		factory.addActionSet("org.eclipse.team.ui.actionSet"); //NON-NLS-1
		factory.addActionSet("org.eclipse.team.cvs.ui.CVSActionSet"); //NON-NLS-1
		factory.addActionSet("org.eclipse.ant.ui.actionSet.presentation"); //NON-NLS-1
		factory.addActionSet(JavaUI.ID_ACTION_SET);
		factory.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);
		factory.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET); //NON-NLS-1
	}

	private void addPerspectiveShortcuts() {
		factory.addPerspectiveShortcut("org.eclipse.team.ui.TeamSynchronizingPerspective"); //NON-NLS-1
		factory.addPerspectiveShortcut("org.eclipse.team.cvs.ui.cvsPerspective"); //NON-NLS-1
		factory.addPerspectiveShortcut("org.eclipse.ui.resourcePerspective"); //NON-NLS-1
	}

	private void addNewWizardShortcuts() {
		factory.addNewWizardShortcut("org.eclipse.team.cvs.ui.newProjectCheckout");//NON-NLS-1
		factory.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//NON-NLS-1
		factory.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//NON-NLS-1
	}

	private void addViewShortcuts() {
		factory.addShowViewShortcut("org.eclipse.ant.ui.views.AntView"); //NON-NLS-1
		factory.addShowViewShortcut("org.eclipse.team.ccvs.ui.AnnotateView"); //NON-NLS-1
		factory.addShowViewShortcut("org.eclipse.pde.ui.DependenciesView"); //NON-NLS-1
		factory.addShowViewShortcut("org.eclipse.jdt.junit.ResultView"); //NON-NLS-1
		factory.addShowViewShortcut("org.eclipse.team.ui.GenericHistoryView"); //NON-NLS-1
		factory.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		factory.addShowViewShortcut(JavaUI.ID_PACKAGES);
		factory.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		factory.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		factory.addShowViewShortcut(IPageLayout.ID_OUTLINE);
	}

}
