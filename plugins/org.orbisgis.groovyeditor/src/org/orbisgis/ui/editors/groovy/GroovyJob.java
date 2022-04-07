/*
 * Groovy Editor (GE) is a library that brings a groovy console to the Eclipse RCP. 
 * GE is developed by CNRS http://www.cnrs.fr/.
 *
 * GE is part of the OrbisGIS project. GE is free software;
 * you can redistribute it and/or modify it under the terms of the GNU Lesser 
 * General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * GE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details http://www.gnu.org/licenses.
 *
 *
 *For more information, please consult: http://www.orbisgis.org
 *or contact directly: info_at_orbisgis.org
 */
package org.orbisgis.ui.editors.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.transform.ThreadInterrupt;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.orbisgis.core.logger.Logger;
import org.orbisgis.ui.editors.groovy.GroovyOutputConsole.GroovyConsoleContent;
import org.orbisgis.ui.editors.groovy.logger.GroovyLogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLClassLoader;

public class GroovyJob extends Job {

    private static final Logger LOGGER = new Logger(GroovyJob.class);

    private String script;
    private GroovyShell shell = null;
    private Binding binding;
    private String name;
    private Thread t;

    public GroovyJob(String name, String script) {
        super(name);
        this.script = script;
        this.name = name;
        binding = new Binding();
        binding.setProperty("logger", new GroovyLogger(GroovyShell.class));
        binding.setProperty("out", new StringWriter());
        
        CompilerConfiguration configuratorConfig = new CompilerConfiguration(System.getProperties());
        configuratorConfig.addCompilationCustomizers(new ASTTransformationCustomizer(ThreadInterrupt.class));
   
        try {
        	URLClassLoader classLoader = new URLClassLoader( ClassPathHandler.getUrlsInArray(), Thread.currentThread().getContextClassLoader() );
        	shell = new GroovyShell(classLoader, binding, configuratorConfig);
        }  catch (Exception e) {
            LOGGER.warn("Unable to create GroovyShell instead.");
        }
    }

    @Override
    protected IStatus run(IProgressMonitor iProgressMonitor) {
        GroovyRunnable run = new GroovyRunnable(shell, name, script, binding);
        t = new Thread(run);
        t.start();
        int status = IStatus.ERROR;
        try {
            t.join();
            status = run.getStatus();
        } catch (InterruptedException e) {
            LOGGER.error("Unable to execute the Groovy script thread.", e);
        } catch (Exception e){
            LOGGER.error("Error while execution the Groovy script.", e);
        }
        Object result = run.getResult();
        String message =  "Groovy script successfully executed.";
        if(result != null){
            message = result.toString();
        }
        if(status == IStatus.OK) {
            LOGGER.info(message);
        }
        return new Status(IStatus.OK, GroovyJob.class.getName(), message);
    }

    @Override
    protected void canceling() {
        super.canceling();
        if(t.isAlive()) {
            t.interrupt();
        }
    }

    private class GroovyRunnable implements Runnable{

        private Object result = null;
        private GroovyShell shell;
        private String script;
        private Binding binding;
        private String name;
        private int status;
        private StringWriter sw = new StringWriter();
        private PrintWriter pw = new PrintWriter(sw);

        public GroovyRunnable(GroovyShell shell, String name, String script, Binding binding){
            this.shell = shell;
            this.script = script;
            this.binding = binding;
            this.name = name;
        }

        @Override
        public void run() {
            try {
                shell.run(script,name, new String[] {});
                String outputStream = shell.getProperty("out").toString();
                GroovyConsoleContent.writeFileNameIntoConsole(name);
                GroovyConsoleContent.writeIntoConsole(outputStream, true);
                GroovyConsoleContent.writeIntoConsole("END");
                LOGGER.trace(outputStream);
                status = IStatus.OK;
            } catch (Exception e){
                e.printStackTrace(pw);
                String sStackTrace = sw.toString();
            	GroovyConsoleContent.writeFileNameIntoConsole(name);
                GroovyConsoleContent.writeIntoConsole(sStackTrace);
                GroovyConsoleContent.writeIntoConsole("BAD_END");
                LOGGER.trace(sStackTrace);   
                status = IStatus.ERROR;
            }
        }

        public Object getResult(){
            return result;
        }

        public int getStatus(){
            return status;
        }
    }

}
