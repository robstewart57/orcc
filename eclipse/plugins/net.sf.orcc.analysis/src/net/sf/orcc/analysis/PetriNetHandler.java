
package net.sf.orcc.analysis;

import java.io.Reader;
import java.io.StringReader;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.parser.IParser;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

import net.sf.orcc.backends.cal.InstancePrinter;
import net.sf.orcc.cal.cal.AstActor;
import net.sf.orcc.cal.cal.AstEntity;
import net.sf.orcc.cal.services.CalGrammarAccess;
import net.sf.orcc.cal.ui.internal.CalActivator;
import net.sf.orcc.df.Actor;
import net.sf.orcc.df.DfFactory;
import net.sf.orcc.frontend.ActorTransformer;
import net.sf.orcc.ir.IrFactory;
import net.sf.orcc.ui.editor.PartialCalParser;
import net.sf.orcc.util.FilesManager;


/**
 * 
 * @author Rob Stewart
 *
 */
public class PetriNetHandler extends AbstractHandler {

	// final private PartialCalParser calParser = new PartialCalParser();
	
	@Inject 
	private Provider<ResourceSet> resourceSetProvider;
	
	@Inject
	private IGenerator generator;
	
	@Inject 
	private JavaIoFileSystemAccess fileAccess;
	
	Injector injector;
	
	@Override
	public boolean isEnabled() { return true; }
	
//	private AstActor parseActor(final String typeString) {
//		Injector injector = CalActivator.getInstance().getInjector("net.sf.orcc.cal.Cal");
//		IParser parser = injector.getInstance(IParser.class);
//		CalGrammarAccess grammarAccess = (CalGrammarAccess) injector.getInstance(IGrammarAccess.class);
//		Reader reader = new StringReader(typeString);
//		// final IParseResult result = parser.parse(grammarAccess.getAstActorRule(), reader);
//		IParseResult result = parser.parse(grammarAccess.getAstEntityRule(), reader);
//		if (result.hasSyntaxErrors()) {
//			for (INode node : result.getSyntaxErrors()) {
//				System.out.println("Syntax error: " + node.getText());
//			}
//			return null;
//		}
//		AstEntity astActor = (AstEntity) result.getRootASTElement();
//		return astActor.getActor();
//	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		/* check for the existance of the fiacre and tina executables */
		// TODO
		
		// get workbench window
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
		
		System.out.println("Petri net handler run");
		
		if (!(editorPart instanceof ITextEditor)) return null;
	    ITextEditor ite = (ITextEditor) editorPart;
	    IDocument doc = ite.getDocumentProvider().getDocument(ite.getEditorInput());
	    
	    
	    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	    
	    
	    //System.out.println(doc.get());
		
	    /* parse actor into an AST */	    
	   PartialCalParser parser = new PartialCalParser();
	   AstActor actorAst = parser.parseActorAst(doc.get());
	    
//	    Actor test = DfFactory.eINSTANCE.createActor();
//	    System.out.println("Generated URI: " + test.eResource().getURI());
	    
//	    ResourceSet resourceSet;
//	    resourceSet = injector.getInstance(ResourceSet.class);
//	    URI uri = URI.createPlatformResourceURI("/actor-gen/rob.cal", true);
//		Resource res = resourceSet.getResource(uri, true);
		
	    
	    
//	    ResourceSet set = resourceSetProvider.get();
//		Resource resource = set.getResource(URI.createURI("newActorName"), true);
//		
//		fileAccess.setOutputPath("actor-gen/");
//		generator.doGenerate(resource, fileAccess);
	    
	    
	    // System.out.println("is the actor null? " + (actor == null));
	    // actorAst.eResource().setURI(URI.createURI("http://dummy.com"));
	   
	    
	    /* map AST to Orcc IR */
	    // Actor actor = null;
	    ActorTransformer trans = new ActorTransformer();
	    System.out.println("IS NULL? " + (actorAst.eResource() == null));
	    Actor actor = (Actor) trans.doSwitch(actorAst);
	    System.out.println("ACTOR NAME: " + actor.getName());
	    
	    
	    
	    
	    /* print IR to Fiacre */
//	    String srcPath = "/tmp/fiacre_out";
//	    InstancePrinter instancePrinter = new InstancePrinter();
//		instancePrinter.setActor(actor);
//		FilesManager.writeFile(instancePrinter.getFileContent(), srcPath, actor.getSimpleName() + ".fcr");
//	
		/* run TINA out the generated file */
	    
	    return null;
	}

}
