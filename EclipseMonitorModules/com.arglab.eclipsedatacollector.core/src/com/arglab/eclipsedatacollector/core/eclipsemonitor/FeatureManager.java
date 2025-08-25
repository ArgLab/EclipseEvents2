package com.arglab.eclipsedatacollector.core.eclipsemonitor;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.*;
import org.eclipse.equinox.p2.metadata.*;
import org.eclipse.equinox.p2.operations.*;
import org.eclipse.equinox.p2.query.*;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.arglab.eclipsedatacollector.core.eclipsemonitor.utils.Utils;

public class FeatureManager {
	
	private static final String UPDATE_SITE_URL = "https://arglab.github.io/EclipseEvents2/eclipse-monitor-update-site";
	private static final String FEATURE_ID_216 = "jenkins.feature.group";
//	private static final String FEATURE_ID_316 = "analysis.feature.group";
	private String course;
	
	public void installFeatureIfNeeded(String newCourse) {
		course = newCourse;
//		continueInstall();
	}
	public void installFeatureIfNeeded() {
		Map<String, String> userPreferences = Utils.getInfo();
		course = userPreferences.get("course");
//		continueInstall();
	}
	
	
	public void continueInstall() {	
//        Map<String, String> userPreferences = Utils.getInfo();
//		System.out.println("------------------------------------------");
//		System.out.println("------------------------------------------");
//		System.out.println("------------------------------------------");
//		System.out.println(course);
//		System.out.println("------------------------------------------");
//		System.out.println("------------------------------------------");
//		System.out.println("------------------------------------------");
		boolean scheduleRestart = false;
//        if (course=="216" && !isFeatureInstalled(FEATURE_ID_216)) {
//            boolean I = installFeature(FEATURE_ID_216);
//            if(I) {
//            	System.out.println("Installed");
//            	scheduleRestart = true;
//            }
//            else {
//            	System.out.println("Not installed");
//            }
//        } else if (course!="216" && isFeatureInstalled(FEATURE_ID_216)) {
//            boolean U = uninstallFeature(FEATURE_ID_216);
//            if(U) {
//	        	System.out.println("Uninstalled");
//	        	scheduleRestart = true;
//            }
//            else {
//	        	System.out.println("Not Uninstalled");
//            }
//        }
//        else {
//        	System.out.println("Something else" + course + isFeatureInstalled(FEATURE_ID_216));
//        }
        if (course=="316" && !isFeatureInstalled(FEATURE_ID_216)) {
            boolean I = installFeature(FEATURE_ID_216);
            if(I) {
            	System.out.println("Installed");
            	scheduleRestart = true;
            }
            else {
            	System.out.println("Not installed");
            }
        } else if (course!="316" && isFeatureInstalled(FEATURE_ID_216)) {
            boolean U = uninstallFeature(FEATURE_ID_216);
            if(U) {
	        	System.out.println("Uninstalled");
	        	scheduleRestart = true;
            }
            else {
	        	System.out.println("Not Uninstalled");
            }
        }
        else {
        	System.out.println("Something else" + course + isFeatureInstalled(FEATURE_ID_216));
        }
        if (scheduleRestart == true) {
        	scheduleRestart();
        }
        else {
        	System.out.println("No need for restart");
        }
    }

	private void scheduleRestart() {
	    Display.getDefault().asyncExec(() -> {
	        boolean proceed = MessageDialog.openQuestion(
	            Display.getDefault().getActiveShell(),
	            "Restart Required",
	            "Eclipse needs to restart to complete plugin installation. Restart now?"
	        );
	        
	        if (proceed) {
	            Job restartJob = new Job("Restart Eclipse") {
	                @Override
	                protected IStatus run(IProgressMonitor monitor) {
	                    Display.getDefault().asyncExec(() -> {
	                        PlatformUI.getWorkbench().restart();
	                    });
	                    return Status.OK_STATUS;
	                }
	            };
	            restartJob.schedule(1000);
	        }
	    });
	}
	
	private boolean isFeatureInstalled(String feature) {
		try {
	        IProvisioningAgent agent = getProvisioningAgent();

	        IProfileRegistry profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
	        IProfile profile = profileRegistry.getProfile(IProfileRegistry.SELF);
	        IQueryResult<IInstallableUnit> result = profile.query(QueryUtil.createIUQuery(feature), new NullProgressMonitor());
	        return !result.isEmpty();
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
    }
	
	private boolean installFeature(String feature) {
		try {
            IProvisioningAgent agent = getProvisioningAgent();
            IMetadataRepositoryManager metadataManager = (IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
            IMetadataRepository repository = metadataManager.loadRepository(URI.create(UPDATE_SITE_URL), null);
            IQueryResult<IInstallableUnit> result = repository.query(QueryUtil.createIUQuery(feature), null);

            if (result.isEmpty()) {
                System.err.println("No IU found with id " + feature);
                return false;
            }
            
            IInstallableUnit unit = result.iterator().next();

            ProvisioningSession session = new ProvisioningSession(agent);
            InstallOperation operation = new InstallOperation(session, Collections.singleton(unit));
            IStatus resolveStatus = operation.resolveModal(new NullProgressMonitor());

            if (!resolveStatus.isOK()) {
                return false;
            }

            IStatus installStatus = operation.getProvisioningJob(null).run(new NullProgressMonitor());
            return installStatus.isOK();
            
        } catch (Exception e) {
        	e.printStackTrace();
            return false;
        }
    }
	
	private boolean uninstallFeature(String feature) {
	    try {
	        IProvisioningAgent agent = getProvisioningAgent();

	        // 1. Locate the IU in the current profile
	        IProfileRegistry profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
	        IProfile selfProfile = profileRegistry.getProfile(IProfileRegistry.SELF);

	        IQueryResult<IInstallableUnit> result = selfProfile.query(QueryUtil.createIUQuery(feature), null);

	        if (result.isEmpty()) {
	            System.err.println("Feature " + feature + " is not installed.");
	            return false;
	        }

	        Collection<IInstallableUnit> toUninstall = new ArrayList<>();
	        result.forEach(toUninstall::add);

	        // 2. Create and resolve the uninstall operation
	        ProvisioningSession session = new ProvisioningSession(agent);
	        UninstallOperation operation = new UninstallOperation(session, toUninstall);

	        IStatus resolveStatus = operation.resolveModal(new NullProgressMonitor());
	        if (!resolveStatus.isOK()) {
	            return false;
	        }

	        // 3. Execute the provisioning job
	        IStatus uninstallStatus =
	                operation.getProvisioningJob(null).run(new NullProgressMonitor());

	        return uninstallStatus.isOK();

	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}
    
    private IProvisioningAgent getProvisioningAgent() {
        return (IProvisioningAgent) org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper.getService(Activator.getDefault().getBundleContext(), IProvisioningAgent.class.getName());
    }

}
