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
import groovy.lang.MissingPropertyException;
import groovy.transform.ThreadInterrupt;
import groovy.util.GroovyScriptEngine;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.orbisgis.core.CoreActivator;
import org.orbisgis.core.logger.Logger;
import org.orbisgis.ui.editors.groovy.logger.GroovyLogger;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GroovyJob extends Job {

    private static final Logger LOGGER = new Logger(GroovyJob.class);

    private String script;
    private GroovyShell shell = null;
    private GroovyScriptEngine engine;
    private Binding binding;
    private String name;
    private Thread t;

    public GroovyJob(String name, String script){
        super(name);
        this.script = script;
        this.name = name;

        binding = new Binding();
        binding.setProperty("logger", new GroovyLogger(GroovyShell.class));

        CompilerConfiguration configuratorConfig = new CompilerConfiguration();
        configuratorConfig.addCompilationCustomizers(new ASTTransformationCustomizer(ThreadInterrupt.class));
        /*
        try {
            String root = CoreActivator.getInstance().getCoreWorkspace().getFolder("Groovy").getLocation().toString() + File.separator;
            System.out.println(root);
            engine = new GroovyScriptEngine(root);
            engine.setConfig(configuratorConfig);
        } catch (IOException e) {
            LOGGER.warn("Unable to create the groovy engine, use GroovyShell instead.");
            shell = new GroovyShell(this.getClass().getClassLoader(), binding, configuratorConfig);
        }
        */
        shell = new GroovyShell(this.getClass().getClassLoader(), binding, configuratorConfig);
        
    }

    @Override
    protected IStatus run(IProgressMonitor iProgressMonitor) {
        GroovyRunnable run = new GroovyRunnable(engine, shell, name, script, binding);
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
        private GroovyScriptEngine engine;
        private Binding binding;
        private String name;
        private int status;

        public GroovyRunnable(GroovyScriptEngine engine, GroovyShell shell, String name, String script, Binding binding){
            this.shell = shell;
            this.script = script;
            this.engine = engine;
            this.binding = binding;
            this.name = name;
        }

        @Override
        public void run() {
            try {
                if(engine != null){
                    result = engine.run(name, binding);
                }
                else {
                    result = shell.evaluate(script);
                }
                status = IStatus.OK;
            } catch (MissingPropertyException e){
                String property = e.getProperty();
                LOGGER.error("Error while execution the Groovy script.", e);
                status = IStatus.ERROR;
            } catch(Exception e){
                LOGGER.error("Error while executing the groovy script.", e);
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
