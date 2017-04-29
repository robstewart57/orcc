
package net.sf.orcc.analysis;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.Model;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.inject.Injector;

import net.sf.orcc.OrccActivator;
import net.sf.orcc.OrccRuntimeException;
import net.sf.orcc.backends.fiacre.InstancePrinter;
import net.sf.orcc.cal.CalStandaloneSetup;
import net.sf.orcc.cal.cal.AstActor;
import net.sf.orcc.cal.cal.AstEntity;
import net.sf.orcc.df.Actor;
import net.sf.orcc.frontend.ActorTransformer;
import net.sf.orcc.ui.OrccUiActivator;
import net.sf.orcc.ui.console.OrccUiConsoleHandler;
import net.sf.orcc.ui.launching.OrccProcess;
import net.sf.orcc.util.FilesManager;
import net.sf.orcc.util.OrccLogger;
import org.eclipse.ui.console.IConsole;

/**
 * 
 * @author Rob Stewart
 *
 */
public class PetriNetHandler extends AbstractHandler {

	ExecutionEvent event;

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// IProcess process=DebugUITools.getCurrentProcess();

//		Job job = new Job("Petri net analysis") {
//			@Override
//			protected IStatus run(IProgressMonitor monitor) {
//				System.out.println("Helllloooooo");
//				return null;
//			}
//
//			@Override
//			protected void canceling() {
//				super.canceling();
//				OrccLogger.traceln("Simulation aborted (from eclipse control).");
//				this.done(Status.OK_STATUS);
//			}
//		};

		// Configure the logger with the console attached to the process
		OrccLogger.configureLoggerWithHandler(
				new OrccUiConsoleHandler(OrccUiActivator.getOrccConsole("Compilation console")));

		/* check for the existance of the fiacre and tina executables */
		/* TODO: return early if any of this fails */
		String fracExec = findExecutableOnPath("frac");
		String fracPath = findSystemVariable("FRACDIR");
		String tinaPath = findSystemVariable("TINADIR");

		if (fracExec.equals("")) {
			OrccLogger.warnln("Unable to find 'frac' executable in PATH");
		} else if (fracPath.equals("")) {
			OrccLogger.warnln("Unable to find 'FRACDIR' system variable");
		} else if (tinaPath.equals("")) {
			OrccLogger.warnln("Unable to find 'TINADIR' system variable");
		} else {

			// get workbench window
			IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
			System.out.println("Petri net handler run");
			// if (!(editorPart instanceof ITextEditor))
			// return null;

			ITextEditor ite = (ITextEditor) editorPart;
			IDocument doc = ite.getDocumentProvider().getDocument(ite.getEditorInput());

			new org.eclipse.emf.mwe.utils.StandaloneSetup().setPlatformUri("../");
			Injector injector = new CalStandaloneSetup().createInjectorAndDoEMFRegistration();
			// LLVM_IRPackage.eINSTANCE.class();
			XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
			resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
			Resource resource = resourceSet.createResource(URI.createURI("dummy:/example.cal"));
			InputStream in = new ByteArrayInputStream(doc.get().getBytes(StandardCharsets.UTF_8));
			try {
				resource.load(in, resourceSet.getLoadOptions());
			} catch (IOException e) {
				e.printStackTrace();
			}
			AstEntity entity = (AstEntity) resource.getContents().get(0);
			ActorTransformer trans = new ActorTransformer();
			Actor actor = (Actor) trans.doSwitch(entity.getActor());
			System.out.println(actor.getName());

			/* print IR to Fiacre */
			String srcPath = "/tmp/fiacre_out/";
			InstancePrinter instancePrinter = new InstancePrinter();
			instancePrinter.setActor(actor);

			String fiacreContent = instancePrinter.getFileContent().toString();

			OrccLogger.traceln(fiacreContent);

			FilesManager.writeFile(fiacreContent, srcPath, actor.getSimpleName() + ".fcr");

			/* run TINA out the generated file */
			int fiacreExitValue;
			try {
				fiacreExitValue = runProcess("Fiacre", fracExec + " " + srcPath + actor.getSimpleName() + ".fcr" + " "
						+ srcPath + actor.getSimpleName() + ".tts");
				System.out.println("frac exit value: " + fiacreExitValue);

				/* Fiacre `frac` succeeded */
				if (fiacreExitValue == 0) {
					OrccLogger.traceln("'frac' successful.");
					OrccLogger.traceln("compiling Fiacre specification...");
					String fracMakeCommand = "make -f " + fracPath + "/Makefile " + "FRACLIB=" + fracPath
							+ "/lib/ --directory=" + srcPath + " " + actor.getSimpleName();
					OrccLogger.traceln(fracMakeCommand);
					int fracMakeExitValue = runProcess("Fiacre make", fracMakeCommand);

					/* if `make` succeeded */
					if (fracMakeExitValue == 0) {
						int tinaExitValue = runProcess("TINA",
								tinaPath + "/tina " + srcPath + actor.getSimpleName() + ".tts");
					}
				} else {
					OrccLogger.traceln("'frac' failed with exit value " + fiacreExitValue);
				}
			}

			catch (TimeoutException e) {
				OrccLogger.traceln("TINA imed out: " + e.getStackTrace().toString());
			}
		}
		return null;
	}

	private int runProcess(String informalName, String command) throws TimeoutException {
		Runtime runtime = Runtime.getRuntime();
		Process process;
		// 5 second timeout, because TINA can loop forever in creating search
		// space for some actors.
		long timeout = 5000;
		try {
			process = runtime.exec(command);

			Worker worker = new Worker(process);
			worker.start();
			try {
				worker.join(timeout);

				InputStream inStream = process.getInputStream();
				int ch;
				StringBuilder sb = new StringBuilder();
				while ((ch = inStream.read()) != -1)
					sb.append((char) ch);
				String stdOut = sb.toString();
				OrccLogger.traceln(stdOut);

				inStream = process.getErrorStream();
				sb = new StringBuilder();
				while ((ch = inStream.read()) != -1)
					sb.append((char) ch);
				stdOut = sb.toString();
				if (!stdOut.startsWith("")) {
					OrccLogger.warnln(stdOut);
				}

				OrccLogger.traceln(informalName + " done.");
				if (worker.exit != null) {
					return process.exitValue();
				}
				// return worker.exit;
				else {
					throw new TimeoutException();
				}
			} catch (InterruptedException ex) {
				worker.interrupt();
				Thread.currentThread().interrupt();
				return -1;
				// throw ex;
			} finally {
				process.destroy();
			}

		} catch (IOException e) {
			OrccLogger.severeln("Unable to run " + informalName + " command: " + command);
			// e.printStackTrace();
			return -1;
		}
	}

	private static String findExecutableOnPath(String name) {
		for (String dirname : System.getenv("PATH").split(File.pathSeparator)) {
			File file = new File(dirname, name);
			if (file.isFile() && file.canExecute()) {
				return file.getAbsolutePath();
			}
		}
		throw new AssertionError("cannot find '" + name + "' executable.");
	}

	private static String findSystemVariable(String name) {
		String varName = System.getenv(name);
		if (varName.length() > 0) {
			return varName;
		}
		throw new AssertionError("cannot find '" + name + "' system variable.");
	}

	private static class Worker extends Thread {
		private final Process process;
		private Integer exit;

		private Worker(Process process) {
			this.process = process;
		}

		public void run() {
			try {
				exit = process.waitFor();
			} catch (InterruptedException ignore) {
				return;
			}
		}
	}

}
