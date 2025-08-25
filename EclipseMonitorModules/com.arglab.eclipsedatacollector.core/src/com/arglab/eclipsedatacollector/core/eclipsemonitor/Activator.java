package com.arglab.eclipsedatacollector.core.eclipsemonitor;

import java.awt.MenuItem;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.ConsoleHandler;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.State;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
//import org.eclipse.equinox.p2.metadata.query.IUPropertyQuery;
//import org.eclipse.equinox.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
//import org.eclipse.swt.internal.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.registry.ViewCategory;
import org.eclipse.ui.internal.views.log.AbstractEntry;
import org.eclipse.ui.internal.views.log.LogEntry;
import org.eclipse.ui.internal.views.log.LogView;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.statushandlers.StatusManager.INotificationListener;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.IViewCategory;
import org.eclipse.ui.views.IViewDescriptor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;


import com.google.gson.Gson;
import com.google.gson.JsonElement;

import com.arglab.eclipsedatacollector.core.eclipsemonitor.handlers.ClientServerConnectionHandlers;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.handlers.listeners.KeyBoardClickListener;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.handlers.listeners.LoggerResourceChangeListener;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.handlers.listeners.MouseClickListener;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.handlers.listeners.PopupWindowListener;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.handlers.listeners.WindowClickListener;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.model.MouseClickData;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.model.ProjectExplorerModel;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.model.UserActionData;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.model.WorkSpaceLog;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.model.jsonmodel.EventDataJsonObject;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.model.jsonmodel.SequentialEventData;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.utils.EventTimeComparator;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.utils.Utils;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements IStartup, ISelectionListener {

	// The plug-in ID
	public static final String PLUGIN_ID = "EclipseMonitor"; //$NON-NLS-1$
	private static final String KEY_NAME = "DeveloperID";
	private static final String CLIENT_KEY = "ClientKey";
	private static final String CLIENT_SECRET = "CLientSecret";
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static ClientServerConnectionHandlers obClientConn;
	private static final String FILE_PATH = "keys.properties";

	private Date lastIneretedErrorLogDateTime = null;
	private static IEclipsePreferences preferences;
//	public StringBuilder keyBoardClickEvents;
	//log tailing state (no UI)
	private final List<WorkSpaceLog> errorLogList = Collections.synchronizedList(new ArrayList<>());
	private volatile long logFileCursor = -1L;
	
	//Scheduler for periodic work (send + log tail)
	private ScheduledExecutorService scheduler;
	private ScheduledFuture<?> sendTask;
	private final AtomicBoolean sendInFlight = new AtomicBoolean(false);
	
	private List<SequentialEventData> listSequntialevents;
	MouseClickListener mouseClickListener;
	KeyBoardClickListener keyBoardClickListener;
	// The shared instance
	private static Activator plugin;
	private BundleContext bundleContext;
	private volatile boolean running = false;
	private static Map<String, String> userPreferences;
	
	//watchservice for checking resource change outside eclipse
	private WatchService watchService;
    private Thread watchThread;
    
    private PopupWindowListener popupListener;

    private static final String KEY_LISTENERS_ADDED = "EM_LISTENERS_ADDED";
    
	/**
	 * The constructor
	 */
	public Activator() {
		listSequntialevents = new ArrayList<>();		
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
        retriveKey();
		plugin = this;
		
		bundleContext = context;
		
		
//		LoggerResourceChangeListener listener = new LoggerResourceChangeListener(keyBoardClickListener);
		LoggerResourceChangeListener listener = new LoggerResourceChangeListener();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);
  
        Display display = PlatformUI.getWorkbench().getDisplay();
        popupListener = new PopupWindowListener(display);
        popupListener.startTrackingPopups();
     // Initialize and start the file system watcher
        startFileSystemWatcher();
        
        //backfill log file once (no UI)
        backfillErrorLogFromFileOnce();
        
        //register selection listeners ONCE for existing & future windows
     // register selection listeners ONCE for existing & future windows (UI thread)
        Display.getDefault().asyncExec(() -> {
            IWorkbench workbench = PlatformUI.getWorkbench();
            for (IWorkbenchWindow w : workbench.getWorkbenchWindows()) {
                addSelectionListener(w); // once
            }
            workbench.addWindowListener(new IWindowListener() {

    			@Override
    			public void windowOpened(IWorkbenchWindow window) {
    				// TODO Auto-generated method stub
    				System.out.println("Current Active window: "+window.getActivePage().getLabel());
    				addSelectionListener(window);

    			}

    			@Override
    			public void windowDeactivated(IWorkbenchWindow window) {
    				// TODO Auto-generated method stub
    				System.out.println("window got deactivated at time." + new Date().toString());
    				System.out.println("activated window: "+window.getWorkbench().getActiveWorkbenchWindow().getPages().getClass().getName());
    				String window_Name = window.getWorkbench().getActiveWorkbenchWindow().getPages().getClass().getName(); 
    				//System.out.println("Time to get activated the window. "+new Date().toString());
    				SequentialEventData sed = new SequentialEventData("Window Deactivated", window_Name);
    				listSequntialevents.add(sed);
    				//could capture the event of idle So we can calculate that

    			}

    			@Override
    			public void windowClosed(IWorkbenchWindow window) {
    				// TODO Auto-generated method stub
    				window.getWorkbench().removeWindowListener(this);
    				saveEventDataAndSendtoServer(workbench);
    				removeSelectionListener(window);
    			}

    			@Override
    			public void windowActivated(IWorkbenchWindow window) {
    				// TODO Auto-generated method stub
    				System.out.println("activated window: "+window.getWorkbench().getActiveWorkbenchWindow().getPages().getClass().getName());
    				String window_Name = window.getWorkbench().getActiveWorkbenchWindow().getPages().getClass().getName(); 
    				//System.out.println("Time to get activated the window. "+new Date().toString());
    				SequentialEventData sed = new SequentialEventData("Window Activated", window_Name);
    				listSequntialevents.add(sed);

    			}
    		});
        });
        		
		// === CHANGED === set up scheduler for 10-min periodic job
	    scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
	        Thread t = new Thread(r, "EclipseMonitor-Scheduler");
	        t.setDaemon(true);
	        return t;
	    });
	    sendTask = scheduler.scheduleAtFixedRate(() -> {
	        if (!sendInFlight.compareAndSet(false, true)) return; // skip overlap
	        try {
	            // Tail new log lines (no UI)
	            readNewErrorLogLines();
	            // Send data
	            saveEventDataAndSendtoServer(PlatformUI.getWorkbench());
	        } catch (Throwable t) {
	            t.printStackTrace();
	        } finally {
	            sendInFlight.set(false);
	        }
	    }, 0, 10, TimeUnit.MINUTES);
	    
	}



	@Override
	public void stop(BundleContext context) throws Exception {
		
		 // Stop the file system watcher
        stopFileSystemWatcher();
        
        if(popupListener !=null) {
        	popupListener.stopTrackingPopups();
        	popupListener = null;
        }
        
        //stop scheduler
        if (sendTask != null) sendTask.cancel(false);
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
		plugin = null;
		super.stop(context);
	}

	
	private void startFileSystemWatcher() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            java.nio.file.Path path = Paths.get(ResourcesPlugin.getWorkspace().getRoot().getLocationURI());
            
            System.out.println(path.toString());
            // Register the path to watch for changes
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            running = true;
            watchThread = new Thread(() -> {
                try {
                    while (running) {
                        // Use poll with timeout so we can exit when running=false
                        WatchKey key = watchService.poll(500, java.util.concurrent.TimeUnit.MILLISECONDS);
                        if (key == null) {
                            continue; // timeout; loop around and check running
                        }

                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            if (kind == StandardWatchEventKinds.OVERFLOW) {
                                continue; // lost events; skip
                            }

                            @SuppressWarnings("unchecked")
                            WatchEvent<java.nio.file.Path> ev = (WatchEvent<java.nio.file.Path>) event;
                            java.nio.file.Path relative = ev.context();      // relative to 'root'
                            java.nio.file.Path absolute = path.resolve(relative);

                            System.out.println("FS event: " + kind.name() + " -> " + absolute);

                            SequentialEventData sev =
                                    new SequentialEventData("External File Change", absolute.toString());
                            listSequntialevents.add(sev);
                        }

                        // If reset fails, the directory is no longer accessible; stop the loop.
                        if (!key.reset()) {
                            break;
                        }
                    }
                } catch (java.nio.file.ClosedWatchServiceException cwse) {
                    // Watcher was closed while blocking/polling: exit quietly.
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // allow graceful exit
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }, "WorkspaceWatchThread");

            watchThread.setDaemon(true); // don’t prevent JVM shutdown
            watchThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	private void stopFileSystemWatcher() {
        try {
            if (watchService != null) {
                watchService.close();
            }
            if (watchThread != null) {
                watchThread.interrupt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public BundleContext getBundleContext() {
		return bundleContext;
	}
	
	/**
	This method is for saving the keys, client information within workspace. 
	Right now we only focused on saving the information in internal memory.**/
	
	public static String retriveKey() {
		preferences = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
//		preferences.remove(KEY_NAME);
		String storekey = getKeyFromFile(KEY_NAME);
		
		if(storekey==null) {
			try {
				obClientConn = new ClientServerConnectionHandlers();
				String[] listStr = obClientConn.connectToServer();
				saveKeysToFile(listStr);
				preferences.put(KEY_NAME, listStr[0]);
				preferences.put(CLIENT_KEY, listStr[1]);
				preferences.put(CLIENT_SECRET, listStr[2]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else {
			preferences.put(KEY_NAME, getKeyFromFile(KEY_NAME));
			preferences.put(CLIENT_KEY, getKeyFromFile(CLIENT_KEY));
			preferences.put(CLIENT_SECRET, getKeyFromFile(CLIENT_SECRET));
		}
		return storekey;
	}
	

	private static String getKeyFromFile(String key) {
		String userHome = System.getProperty("user.home");	
		IPath filePath = new Path(FILE_PATH);
		IPath userHomePath = new Path(userHome);

		IPath absolutePath = userHomePath.append(filePath);
		String absolutePathString = absolutePath.toOSString();
		if (!absolutePath.toFile().exists()) {
			return null;
		}
        try (InputStream input = new FileInputStream(absolutePathString)) {
            Properties prop = new Properties();

            // Load properties from file
            prop.load(input);

            // Retrieve the value associated with the specified key
            return prop.getProperty(key);
        } catch (Exception  io) {
            io.printStackTrace();
            return null;
        }
    }
	
	private static void saveKeysToFile(String[] keys) {
		String userHome = System.getProperty("user.home");	
		IPath filePath = new Path(FILE_PATH);
		IPath userHomePath = new Path(userHome);
		IPath absolutePath = userHomePath.append(filePath);
		String absolutePathString = absolutePath.toOSString();
		try (OutputStream output = new FileOutputStream(absolutePathString)) {
			Properties prop = new Properties();

			// Store key-value pairs
			prop.setProperty(KEY_NAME, keys[0]);
			prop.setProperty(CLIENT_KEY, keys[1]);
			prop.setProperty(CLIENT_SECRET, keys[2]);

			// Save properties to file
			prop.store(output, null);

			System.out.println("Keys saved to " + absolutePathString);
		} catch (Exception io) {
			System.out.println("Exception Happened due to: " + io.getMessage());
		}
	}

	
	@Override
	public void earlyStartup() {
		// TODO Auto-generated method stub
		
		try {
            Bundle bundle = Platform.getBundle("com.arglab.eclipsedatacollector.core");
            if (bundle != null && bundle.getState() != Bundle.ACTIVE) {
                bundle.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		
//		Job initJob = new Job("Initialize Feature Management") {
//            @Override
//            protected IStatus run(IProgressMonitor monitor) {
//                FeatureManager manager = new FeatureManager();
//                manager.installFeatureIfNeeded();
//                return Status.OK_STATUS;
//            }
//        };
//        
//        initJob.schedule(2000);
        
        System.out.println("Early startup: Feature management scheduled");
		
		System.out.println("Plugin Started automatically");
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		System.out.println("DESCRIPTION: " + workspace.getRoot().getLocation().toOSString());
		
		ICommandService commandService = workbench.getService(ICommandService.class);
		// Example: Record a user action when a command is executed
		commandService.addExecutionListener(new IExecutionListener() {

			@Override
			public void preExecute(String pr1, ExecutionEvent event) {
				// TODO Auto-generated method stub
				Command command = event.getCommand();
				System.out.println("inside the addExecutionListener");
				String CommandId = command.getId();
				recordUserAction(CommandId, commandService, workbench);

		}

			@Override
			public void postExecuteSuccess(String arg0, Object arg1) {
				// TODO Auto-generated method stub
				System.out.println("Post execution success.");

			}

			@Override
			public void postExecuteFailure(String arg0, ExecutionException arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void notHandled(String arg0, NotHandledException arg1) {
				// TODO Auto-generated method stub

			}
		});

		ConsolePlugin.getDefault().getConsoleManager().addConsoleListener(new IConsoleListener() {

			@Override
			public void consolesRemoved(IConsole[] consoles) {
				// TODO Auto-generated method stub

				for (IConsole console : consoles) {
					if (console instanceof TextConsole) {
						System.out.println("remove console from this.");
						// ((TextConsole) console).removePatternMatchListener(this);
					}
				}
			}

			@Override
		    public void consolesAdded(IConsole[] consoles) {
		        // marshal to UI thread to avoid Invalid thread access during console notifications
		        Display.getDefault().asyncExec(() -> {
		            for (IConsole console : consoles) {
		                System.out.println("console output: " + console.getName());
		                if (console instanceof TextConsole) {
		                    try {
		                        TextConsole textConsole = (TextConsole) console;
		                        IDocument document = textConsole.getDocument(); // safe on UI thread
		                        String consoleContents = document != null ? document.get() : "";
		                        System.out.println("Console Contents:");
		                        System.out.println(consoleContents);
		                        listSequntialevents.add(
		                            new SequentialEventData("ConsoleOutputEvent", consoleContents)
		                        );
		                    } catch (Throwable t) {
		                        // never throw from listener; just log
		                        t.printStackTrace();
		                    }
		                }
		            }
		        });
		    }
		});
	
	}

	// backfill once at startup from .metadata/.log (no UI)
	private void backfillErrorLogFromFileOnce() {
	    try {
	        IPath ws = ResourcesPlugin.getWorkspace().getRoot().getLocation();
	        java.nio.file.Path logPath = ws.append(".metadata").append(".log").toFile().toPath();
	        if (!Files.exists(logPath)) return;

	        long size = Files.size(logPath);
	        // read entire file first time
	        readLogChunk(logPath, 0, size, /*addToBuffer=*/true);
	        logFileCursor = size;
	    } catch (Exception ignore) {
	        System.out.println("Exception Happening from backfillErrorLogFromFileOnce because: "+ignore.getMessage());
	    }
	}
	
	//read only new bytes into buffer (called on each tick)
	private void readNewErrorLogLines() {
	    try {
	        IPath ws = ResourcesPlugin.getWorkspace().getRoot().getLocation();
	        java.nio.file.Path logPath = ws.append(".metadata").append(".log").toFile().toPath();
	        if (!Files.exists(logPath)) return;

	        long size = Files.size(logPath);
	        if (logFileCursor < 0) {
	            // in case backfill didn't run
	            backfillErrorLogFromFileOnce();
	            return;
	        }
	        if (size <= logFileCursor) return; // nothing new

	        readLogChunk(logPath, logFileCursor, size, /*addToBuffer=*/true);
	        logFileCursor = size;
	    } catch (Exception ignore) {
	    	System.out.println("Exception Happening from readNewErrorLogLines because: "+ignore.getMessage());
	    }
	}
	
	// minimal parser: convert lines to WorkSpaceLog
	private void readLogChunk(java.nio.file.Path logPath, long start, long end, boolean addToBuffer) throws Exception {
	    if (end <= start) return;
	    try (SeekableByteChannel ch = Files.newByteChannel(logPath, StandardOpenOption.READ)) {
	        ch.position(start);
	        ByteBuffer buf = ByteBuffer.allocate(8192);
	        StringBuilder sb = new StringBuilder();
	        long toRead = end - start;
	        while (toRead > 0) {
	            int n = ch.read(buf);
	            if (n <= 0) break;
	            toRead -= n;
	            buf.flip();
	            sb.append(StandardCharsets.UTF_8.decode(buf));
	            buf.clear();
	        }

	        String[] lines = sb.toString().split("\\R");
	        String currentSessionData = null;
	        for (int i = 0; i < lines.length; i++) {
	            String line = lines[i];
	            if (line == null || line.isEmpty()) continue;

	            // --- Capture a !SESSION block (title + subsequent property lines) ---
	            
	            if (line.startsWith("!SESSION")) {
	                // Example: "!SESSION 2025-08-19 20:45:33.724 -----------------------------------------------"
	                StringBuilder sess = new StringBuilder();
	                sess.append(line);

	                // slurp subsequent lines until the next !ENTRY / !SESSION / !MESSAGE / !SUBENTRY / !STACK or dashed line
	                int j = i + 1;
	                while (j < lines.length) {
	                    String l2 = lines[j];
	                    if (l2 == null) break;
	                    String t = l2.trim();

	                    if (t.startsWith("!ENTRY") || t.startsWith("!SESSION") ||
	                        t.startsWith("!MESSAGE") || t.startsWith("!SUBENTRY") ||
	                        t.startsWith("!STACK") || t.startsWith("--------------------------------")) {
	                        break;
	                    }

	                    if (!t.isEmpty()) {
	                        sess.append("\n").append(l2);
	                    }
	                    j++;
	                }

	                currentSessionData = sess.toString();
	                i = j - 1; // advance outer loop to where we stopped
	                continue;
	            }

	            // --- Infer severity & plugin id (lightweight) ---
	            String severity = "INFO";
	            String pluginId = null;

	            if (line.startsWith("!ENTRY")) {
	                // Format: !ENTRY <pluginId> <severityNum> <code> <timestamp...>
	                String[] parts = line.split("\\s+");
	                if (parts.length >= 3) {
	                    pluginId = parts[1];
	                    String sevNum = parts[2];
	                    if ("4".equals(sevNum))       severity = "ERROR";
	                    else if ("2".equals(sevNum) ||
	                             "1".equals(sevNum))  severity = "WARN";
	                    else                           severity = "INFO";
	                }
	            } else if (line.contains("ERROR")) {
	                severity = "ERROR";
	            } else if (line.contains("WARN")) {
	                severity = "WARN";
	            }

	            // --- Build your log object, including the current session block ---
	            WorkSpaceLog wsl = new WorkSpaceLog(new Date(), severity, pluginId, line, currentSessionData);

	            // NOTE: use whichever buffer you actually send with (errorLogBuffer vs errorLogList)
	            if (addToBuffer) {
	                // If you adopted the buffered approach:
	                // errorLogBuffer.add(wsl);
	                // If you kept errorLogList:
	                errorLogList.add(wsl);
	            }
	        }
	    }
	}

	
	public void saveEventDataAndSendtoServer(IWorkbench workbench) {
		
		List<WorkSpaceLog> logsToSend;
	    synchronized (errorLogList) {
	        logsToSend = new ArrayList<>(errorLogList);
	        errorLogList.clear();
	    }
//	    this.errorLogList = logsToSend; // keep your existing payload structure happy
	
		//Getting OS Version
		String osName = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		String osArch = System.getProperty("os.arch");
		
		//Getting Java Version
		String javaVersion = System.getProperty("java.version");
		String javaVendor = System.getProperty("java.vendor");
		
		//Getting Eclipse Version
		String eclipseVersion = Platform.getBundle("org.eclipse.core.runtime").getVersion().toString();
		
		List<SequentialEventData> mousEventList = new ArrayList<>();
		List<SequentialEventData> keyEvents = new ArrayList<>();
		if(mouseClickListener!=null) {
			mousEventList = mouseClickListener.getMouseClickData();
			mouseClickListener.setMouseClickData(new ArrayList<>());
		}
		if(keyBoardClickListener!=null) {
			keyEvents = keyBoardClickListener.getKeboardClickData();
			keyBoardClickListener.setKeboardClickData(new ArrayList<>());
		}
		
		listSequntialevents.addAll(mousEventList);
		listSequntialevents.addAll(keyEvents);
		
		//Grab from global
		listSequntialevents.addAll(GlobalVars.listSequentialEvents);
		
		
		
		//sort based on the event time
		Collections.sort(listSequntialevents, new EventTimeComparator());
		
		EventDataJsonObject edjo = new EventDataJsonObject(listSequntialevents,logsToSend);
//		System.out.println("EDJO"+edjo);
		edjo.setIPAddress(Utils.getIpAddress());
		edjo.setMACAddress(Utils.getMacAddress());
		edjo.setPluginVersion("V2.0.2");
		edjo.setOSInfo(osName, osVersion, osArch);
		edjo.setJavaInfo(javaVersion, javaVendor);
		edjo.setEclipseInfo(eclipseVersion);
		System.out.println("retrived key would be: ");
		Gson gson = new Gson();
		try {
			JsonElement jsonString = gson.toJsonTree(edjo);
			//file save code
			IPath workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
			String workingDir = new Date().getTime()+".json";
			IPath filePath = new Path(workingDir);
			IPath absolutePath = workspaceLocation.append(filePath);
			String absolutePathString = absolutePath.toOSString();
			
			System.out.println("SAVED NEW DATA");
			FileWriter fileWriter = new FileWriter(absolutePathString);
			fileWriter.write(jsonString.toString());
			fileWriter.close();
			/**File send to Server code from Activator**/
			try {
				if(obClientConn!=null) {
					System.out.println("Key:"+preferences.get(KEY_NAME, null));
					obClientConn.sendencryptedMessage(absolutePathString, preferences.get(KEY_NAME, null), 
							preferences.get(CLIENT_SECRET, null), preferences.get(CLIENT_KEY, null));
					
				}
				else {
					retriveKey();
					System.out.println("Key:"+preferences.get(KEY_NAME, null));
					obClientConn = new ClientServerConnectionHandlers();
					obClientConn.sendencryptedMessage(absolutePathString, preferences.get(KEY_NAME, null), 
							preferences.get(CLIENT_SECRET, null), preferences.get(CLIENT_KEY, null));
				}
				checkanyRemainingFilestoSend(workspaceLocation.toOSString());
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Exception Happened in sending data to server due to :"+e.getMessage());		
			
			}
			/**End of File send to Server**/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Exception happened. "+e.getMessage());
		}
		
		System.out.println("Timer Task has been called.");
		System.out.println("The saved Data is: ");
		System.out.println(edjo.toString());
		//resetVariable
		reset(workbench.getActiveWorkbenchWindow());
	}
	private void checkanyRemainingFilestoSend(String absolutePathString) {
		// TODO Auto-generated method stub
//		IContainer workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		File directory = new File(absolutePathString);

        // Filename filter for .json files
        FilenameFilter jsonFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".json");
            }
        };

        // List all the JSON files in the directory
        File[] files = directory.listFiles(jsonFilter);
        if (files != null) {
            for (File file : files) {
                // Process each file (e.g., print the file name)
//            	System.out.println("Found JSON file: " + file.getName());
//            	IFile filetoSend = (IFile) file;
                String absolutePath = file.getAbsolutePath();
                
                if(obClientConn!=null) {
					obClientConn.sendencryptedMessage(absolutePath, preferences.get(KEY_NAME, null), 
							preferences.get(CLIENT_SECRET, null), preferences.get(CLIENT_KEY, null));
					
				}
				else {
					retriveKey();
					obClientConn.sendencryptedMessage(absolutePath, preferences.get(KEY_NAME, null), 
							preferences.get(CLIENT_SECRET, null), preferences.get(CLIENT_KEY, null));
				}
                // Add your code here to read or process the JSON file
            }
        } else {
            System.out.println("Directory does not exist or is not a directory");
        }
	}

	private void recordUserAction(String commandId, ICommandService commandService, IWorkbench workbench) {
	    try {
	        // Non-UI: record the command ID immediately
	        SequentialEventData menuSED = new SequentialEventData("MenuBarClickEvent", commandId);
	        listSequntialevents.add(menuSED);
	        System.out.println("Recorded action: " + commandId);

	        // All UI work must be inside asyncExec
	        Display display = workbench.getDisplay();
	        if (display == null || display.isDisposed()) return;

	        display.asyncExec(() -> {
	            try {
	                // Make sure our keyboard listener flush runs on UI too
	                if (keyBoardClickListener != null) {
	                    keyBoardClickListener.immediateSave();
	                }

	                if (commandId.contains("copy")) {
	                    display.timerExec(200, () -> {
	                    	Clipboard clipboard = new Clipboard(display);
	                        try{
	                            Object contents = clipboard.getContents(TextTransfer.getInstance());
	                            if (contents instanceof String) {
	                                UserActionData uad = new UserActionData(
	                                        contents.toString(), GlobalVars.lastOpenFile, GlobalVars.activeProject);
	                                listSequntialevents.add(new SequentialEventData("CopyEvent", uad));
	                                System.out.println(uad);
	                            }
	                        } catch (Throwable ignored) {}
	                    });
	                } else if (commandId.contains("cut")) {
	                    display.timerExec(200, () -> {
	                    	Clipboard clipboard = new Clipboard(display);
	                        try{
	                            Object contents = clipboard.getContents(TextTransfer.getInstance());
	                            if (contents instanceof String) {
	                                UserActionData uad = new UserActionData(
	                                        contents.toString(), GlobalVars.lastOpenFile, GlobalVars.activeProject);
	                                listSequntialevents.add(new SequentialEventData("CutEvent", uad));
	                                System.out.println(uad);
	                            }
	                        } catch (Throwable ignored) {}
	                    });
	                } else if (commandId.contains("paste")) {
	                    display.timerExec(200, () -> {
	                    	Clipboard clipboard = new Clipboard(display);
	                        try{
	                            Object contents = clipboard.getContents(TextTransfer.getInstance());
	                            if (contents instanceof String) {
	                                UserActionData uad = new UserActionData(
	                                        contents.toString(), GlobalVars.lastOpenFile, GlobalVars.activeProject);
	                                listSequntialevents.add(new SequentialEventData("PasteEvent", uad));
	                                System.out.println(uad);
	                            }
	                        } catch (Throwable ignored) {}
	                    });
	                } else if (commandId.contains("selectAll")) {
	                    try {
	                        IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	                        if (ww != null && ww.getActivePage() != null) {
	                            IEditorPart ep = ww.getActivePage().getActiveEditor();
	                            if (ep instanceof ITextEditor) {
	                                ITextEditor editor = (ITextEditor) ep;
	                                IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
	                                String selectedText = (doc != null) ? doc.get() : "";
	                                UserActionData uad = new UserActionData(selectedText, GlobalVars.lastOpenFile, GlobalVars.activeProject);
	                                listSequntialevents.add(new SequentialEventData("selectAll", uad));
	                                System.out.println(uad);
	                            }
	                        }
	                    } catch (Throwable ignored) {}
	                } else if (commandId.contains("delete")) {
	                    try {
	                        IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	                        if (ww != null && ww.getActivePage() != null) {
	                            IEditorPart ep = ww.getActivePage().getActiveEditor();
	                            if (ep instanceof ITextEditor) {
	                                ITextEditor editor = (ITextEditor) ep;
	                                IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
	                                String deletedText = (doc != null) ? doc.get() : "";
	                                UserActionData uad = new UserActionData(deletedText, GlobalVars.lastOpenFile, GlobalVars.activeProject);
	                                listSequntialevents.add(new SequentialEventData("Delete", uad));
	                                System.out.println(uad);
	                            }
	                        }
	                    } catch (Throwable ignored) {}
	                }
	            } catch (Throwable uiEx) {
	                showErrorDialog("Error recording user action (UI)", uiEx);
	            }
	        });
	    } catch (Throwable e) {
	        showErrorDialog("Error recording user action", e);
	    }
	}

//	private void recordUserAction(String commandId, ICommandService commandService, IWorkbench workbench) {
//		try {
//			System.out.println("Activator");
//			keyBoardClickListener.immediateSave();
//			Map<String, String> parameters = new HashMap<>();
//			String [] commandStr = commandId.split("\\.");
//			String activePart = commandStr[commandStr.length-2];
//			
//			// parameters.put(commandId, pr1)
//			Command command = commandService.getCommand(commandId);
//			SequentialEventData menuSED = new  SequentialEventData("MenuBarClickEvent", commandId);
//			listSequntialevents.add(menuSED);
////			MenuBarClickActions.add(new MenuBarClickData(commandId));
//			System.out.println("Recorded action: " + commandId);
//
//			if (commandId.contains("copy")) {
//				System.out.println("This is a copy event:");
//				Display display = workbench.getDisplay();
//				display.timerExec(200, new Runnable() { // adding delay on the code to get the current copy event
//
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						Clipboard clipboard = new Clipboard(display);
//						Object contents = clipboard.getContents(TextTransfer.getInstance());
//						clipboard.dispose();
//						if (contents instanceof String) {
////							String key = "copy Event " + copyCounter.toString();
//							UserActionData uad = new UserActionData(contents.toString(), GlobalVars.lastOpenFile, GlobalVars.activeProject);
//							SequentialEventData menuCopy = new  SequentialEventData("CopyEvent", uad);
//							listSequntialevents.add(menuCopy);
////							cutcopyPasteEvents.add(new CutCopyPasteEvent("Copy", contents.toString()));
//							System.out.println(uad);
//						}
//					}
//				});
//			} else if (commandId.contains("cut")) {
//				System.out.println("This is a cut event:");
//				Display display = workbench.getDisplay();
//				display.timerExec(200, new Runnable() { // adding delay on the code to get the current copy event
//
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						Clipboard clipboard = new Clipboard(display);
//						Object contents = clipboard.getContents(TextTransfer.getInstance());
//						clipboard.dispose();
//
//						if (contents instanceof String) {
//							UserActionData uad = new UserActionData(contents.toString(), GlobalVars.lastOpenFile, GlobalVars.activeProject);
//							SequentialEventData menuCut = new  SequentialEventData("CutEvent", uad);
//							listSequntialevents.add(menuCut);
////							cutcopyPasteEvents.add(new CutCopyPasteEvent("Cut", contents.toString()));
//							System.out.println(uad);
//						}
//					}
//				});
//			} else if (commandId.contains("paste")) {
//				System.out.println("This is a paste event:");
//				Display display = workbench.getDisplay();
//				display.timerExec(200, new Runnable() { // adding delay on the code to get the current copy event
//
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						Clipboard clipboard = new Clipboard(display);
//						Object contents = clipboard.getContents(TextTransfer.getInstance());
//						clipboard.dispose();
//
//						if (contents instanceof String) {
//							UserActionData uad = new UserActionData(contents.toString(), GlobalVars.lastOpenFile, GlobalVars.activeProject);
//							SequentialEventData menuPaste = new  SequentialEventData("PasteEvent", uad);
//							listSequntialevents.add(menuPaste);
////							cutcopyPasteEvents.add(new CutCopyPasteEvent("Paste", contents.toString()));
//							System.out.println(uad);
//						}
//					}
//				});
//			} else if (commandId.contains("selectAll")) {
//				System.out.println("This is a Select All event:");
//				ITextEditor editor = (ITextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
//						.getActiveEditor();
//				IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
//				String selectedText = document.get();
//				System.out.println("Selected Text: " + selectedText);
//				// Now you can use 'selectedText' as the contents of the select all action
//				UserActionData uad = new UserActionData(selectedText, GlobalVars.lastOpenFile,
//						GlobalVars.activeProject);
//				SequentialEventData menuSelectAll = new SequentialEventData("selectAll", uad);
//				listSequntialevents.add(menuSelectAll);
//				System.out.println(uad);
//			} else if (commandId.contains("delete")) {
//				System.out.println("This is a delete event:");
//				ITextEditor editor = (ITextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
//						.getActiveEditor();
//				IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
//				String deletedText = document.get();
//				System.out.println("Selected Text: " + deletedText);
//				UserActionData uad = new UserActionData(deletedText, GlobalVars.lastOpenFile,
//						GlobalVars.activeProject);
//				SequentialEventData menuDelete= new SequentialEventData("Delete", uad);
//				listSequntialevents.add(menuDelete);
//				System.out.println(uad);
//			}
//
//		} catch (Exception e) {
//			showErrorDialog("Error recording user action", e);
//		}
//	}

	private void showErrorDialog(String message, Throwable throwable) {
	    IStatus status = new Status(IStatus.ERROR, getBundle().getSymbolicName(), message, throwable);
	    Display display = Display.getDefault();
	    if (display == null || display.isDisposed()) return;
	    display.asyncExec(() -> {
	        if (!display.isDisposed()) {
	            ErrorDialog.openError(null, "Error", null, status);
	        }
	    });
	}


	private final ISelectionListener globalSelectionListener = (part, selection) -> {
	    handleSelectionChange(part, selection);
	};
	
	private void addSelectionListener(IWorkbenchWindow window) {
	    if (window == null) return;

	    Display display = window.getShell().getDisplay();
	    Runnable register = () -> {
	        if (window.getShell() == null || window.getShell().isDisposed()) return;

	        // avoid duplicates on the same window
	        if (Boolean.TRUE.equals(window.getShell().getData(KEY_LISTENERS_ADDED))) return;

	        // set up your listeners once for this window
	        keyBoardClickListener = new KeyBoardClickListener(window);
	        mouseClickListener   = new MouseClickListener(window, keyBoardClickListener);

	        display.addFilter(org.eclipse.swt.SWT.MouseDown,        mouseClickListener);
	        display.addFilter(org.eclipse.swt.SWT.MouseDoubleClick, mouseClickListener);
	        display.addFilter(org.eclipse.swt.SWT.KeyDown,          keyBoardClickListener);

	        // use *post* selection to get final selection after UI settles
	        window.getSelectionService().addPostSelectionListener(globalSelectionListener);

	        // safer: use this window's PartService instead of re-fetching the active window
	        window.getPartService().addPartListener(new WindowClickListener(
	                listSequntialevents, window, keyBoardClickListener));

	        // mark as wired
	        window.getShell().setData(KEY_LISTENERS_ADDED, Boolean.TRUE);
	    };

	    // run on UI thread
	    if (Display.getCurrent() == display) {
	        register.run();
	    } else {
	        display.asyncExec(register);
	    }
	}

	
	private void handleSelectionChange(IWorkbenchPart part, ISelection selection) {
		// TODO Auto-generated method stub
//		System.out.println("Selection Happened in: "+part.getTitle());
		System.out.println("Selection Happened in: " + (part != null ? part.getTitle() : "<unknown>"));
		
		if (selection == null) {
	        System.out.println("Selection is null.");
	        return;
	    }

	    // --- A) Text inside an editor (e.g., selecting "dfdsdfsdnfsdfsdjfkljl ...") ---
	    if (selection instanceof ITextSelection) {
	        ITextSelection ts = (ITextSelection) selection;

	        // Ignore caret-only moves if you want only real selections
	        if (ts.getLength() <= 0) {
	            System.out.println("Text selection length = 0 (caret move).");
	            return;
	        }

	        String fileName = "<unknown>";
	        String projectName = "<unknown>";
	        String selectedText = null;
	        int line = ts.getStartLine() + 1;  // 1-based
	        int column = 1;

	        try {
	            // Try to get the active editor’s document to extract the text & column
	            ITextEditor textEditor = part.getAdapter(ITextEditor.class);
	            if (textEditor == null && part instanceof IEditorPart) {
	                // Some editors don’t adapt directly—try site’s selection provider later
	                textEditor = (ITextEditor) ((IEditorPart) part).getAdapter(ITextEditor.class);
	            }

	            if (textEditor != null) {
	                IDocumentProvider provider = textEditor.getDocumentProvider();
	                IDocument doc = provider.getDocument(textEditor.getEditorInput());

	                // Compute 1-based column from offset within the line
	                try {
	                    IRegion lineInfo = doc.getLineInformation(ts.getStartLine());
	                    column = (ts.getOffset() - lineInfo.getOffset()) + 1;
	                } catch (org.eclipse.jface.text.BadLocationException ignore) {}

	                // Extract selected text safely
	                int offset = ts.getOffset();
	                int length = ts.getLength();
	                if (offset >= 0 && length > 0 && offset + length <= doc.getLength()) {
	                    selectedText = doc.get(offset, length);
	                }

	                // Derive file name / project name
	                IEditorInput input = textEditor.getEditorInput();
	                if (input instanceof IFileEditorInput) {
	                    IFile file = ((IFileEditorInput) input).getFile();
	                    if (file != null) {
	                        fileName = file.getName();
	                        IProject p = file.getProject();
	                        if (p != null) projectName = p.getName();
	                    }
	                } else if (input instanceof IStorageEditorInput) {
	                    IStorage storage = ((IStorageEditorInput) input).getStorage();
	                    if (storage != null) fileName = storage.getName();
	                }
	            }
	        } catch (Throwable t) {
	            t.printStackTrace();
	        }

	        System.out.println("Editor text selection in file: " + fileName + " (project: " + projectName + ")");
	        System.out.println("Line: " + line + ", Column: " + column);
	        if (selectedText != null) {
	            System.out.println("Selected text: \"" + selectedText + "\"");
	        }

	        // Record your event
	        ProjectExplorerModel pem = new ProjectExplorerModel();
	        pem.setMouseClick(fileName);
	        pem.setPathClick(projectName);

	        SequentialEventData sed = new SequentialEventData("EditorTextSelection",
	                new MouseClickData(column, line, fileName, line, column)); // or define a dedicated model
	        listSequntialevents.add(sed);
	        return;
	    }
	    
		 // Handle structured selections like those from the Project Explorer
		if (selection instanceof IStructuredSelection) {
		    IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		    Object selectedElement = structuredSelection.getFirstElement();

		    if (selectedElement == null) {
		        System.out.println("No element selected.");
		        return;
		    }

		    // Check if the element is a Project
		    if (selectedElement instanceof IProject) {
		        IProject project = (IProject) selectedElement;
		        if (project != null) {
		            System.out.println("Project Explorer > Project selected: " + project.getName());
		            ProjectExplorerModel pem = new ProjectExplorerModel();
		            pem.setMouseClick(project.getName());
		            pem.setPathClick(project.getName());
		            SequentialEventData sed = new SequentialEventData("MouseClick Project Explorer", pem);
		            listSequntialevents.add(sed);
		        }
		    } 
		    // Check if the element is a Folder
		    else if (selectedElement instanceof IFolder) {
		        IFolder folder = (IFolder) selectedElement;
		        if (folder != null) {
		            String projectName = folder.getProject() != null ? folder.getProject().getName() : "Unknown project";
		            ProjectExplorerModel pem = new ProjectExplorerModel();
		            pem.setMouseClick(folder.getName());
		            pem.setPathClick(projectName);
		            SequentialEventData sed = new SequentialEventData("MouseClick Project Explorer", pem);
		            listSequntialevents.add(sed);
		            System.out.println("Project Explorer > Project: " + projectName + " > Folder selected: " + folder.getName());
		        }
		    } 
		    // Check if the element is a File
		    else if (selectedElement instanceof IFile) {
		        IFile file = (IFile) selectedElement;
		        if (file != null) {
		            String projectName = file.getProject() != null ? file.getProject().getName() : "Unknown project";
		            System.out.println("Project Explorer > Project: " + projectName + " > File selected: " + file.getName());
		            ProjectExplorerModel pem = new ProjectExplorerModel();
		            pem.setMouseClick(file.getName());
		            pem.setPathClick(projectName);
		            SequentialEventData sed = new SequentialEventData("MouseClick Project Explorer", pem);
		            listSequntialevents.add(sed);
		        }
		    } 
		    // Check for Java-specific elements (e.g., Package or Class elements in Project Explorer)
		    else if (selectedElement instanceof org.eclipse.jdt.core.IPackageFragment) {
		        org.eclipse.jdt.core.IPackageFragment pkg = (org.eclipse.jdt.core.IPackageFragment) selectedElement;
		        String projectName = pkg.getJavaProject() != null ? pkg.getJavaProject().getElementName() : "Unknown project";
		        System.out.println("Project Explorer > Project: " + projectName + " > Package selected: " + pkg.getElementName());
		        ProjectExplorerModel pem = new ProjectExplorerModel();
	            pem.setMouseClick(pkg.getElementName());
	            pem.setPathClick(projectName);
	            SequentialEventData sed = new SequentialEventData("MouseClick Project Explorer", pem);
	            listSequntialevents.add(sed);
		    }
		    else if (selectedElement instanceof org.eclipse.jdt.core.IPackageFragmentRoot) {
		        org.eclipse.jdt.core.IPackageFragmentRoot pkgRoot = (org.eclipse.jdt.core.IPackageFragmentRoot) selectedElement;
		        String projectName = pkgRoot.getJavaProject() != null ? pkgRoot.getJavaProject().getElementName() : "Unknown project";
		        System.out.println("Project Explorer > Project: " + projectName + " > Source/Library root selected: " + pkgRoot.getElementName());
		        ProjectExplorerModel pem = new ProjectExplorerModel();
	            pem.setMouseClick(pkgRoot.getElementName());
	            pem.setPathClick(projectName);
	            SequentialEventData sed = new SequentialEventData("MouseClick Project Explorer", pem);
	            listSequntialevents.add(sed);
		    }
		    else if (selectedElement instanceof IJavaElement) {
		    	IJavaElement javaElement = (IJavaElement) selectedElement;
		    	IProject project = javaElement.getJavaProject().getProject();
		        System.out.println("Project Explorer > Project: " + project.getName());

		        // Retrieve the package fragment if it exists
		        IPackageFragment packageFragment = (IPackageFragment) javaElement.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
		        if (packageFragment != null) {
		            System.out.println("Project Explorer > Project: " + project.getName() +
		                               " > Package: " + packageFragment.getElementName());
		        }

		        // Retrieve the package fragment root (source folder or library root) if it exists
		        IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) javaElement.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		        if (packageFragmentRoot != null) {
		            System.out.println("Project Explorer > Project: " + project.getName() +
		                               " > Source/Library root: " + packageFragmentRoot.getElementName());
		        }

		        // Handle file-level elements, such as compilation units (Java files)
		        if (javaElement instanceof ICompilationUnit) {
		            ICompilationUnit compilationUnit = (ICompilationUnit) javaElement;
		            System.out.println("Project Explorer > Project: " + project.getName() +
		                               " > Source/Library root: " + (packageFragmentRoot != null ? packageFragmentRoot.getElementName() : "Unknown root") +
		                               " > Package: " + (packageFragment != null ? packageFragment.getElementName() : "Unknown package") +
		                               " > File: " + compilationUnit.getElementName());
		            
		            ProjectExplorerModel pem = new ProjectExplorerModel();
		            pem.setMouseClick(compilationUnit.getElementName());
		            pem.setPathClick(project.getName());
		            SequentialEventData sed = new SequentialEventData("MouseClick Project Explorer", pem);
		            listSequntialevents.add(sed); 
		        } else {
		        	ProjectExplorerModel pem = new ProjectExplorerModel();
		            pem.setMouseClick(javaElement.getElementName());
		            pem.setPathClick(project.getName());
		            SequentialEventData sed = new SequentialEventData("MouseClick Project Explorer", pem);
		            listSequntialevents.add(sed); 
		            System.out.println("Selected element is a Java element but not a compilation unit: " + javaElement.getElementName());
		        }
		    }
		    // Log the class type of unknown elements
		    else {
		    	ProjectExplorerModel pem = new ProjectExplorerModel();
	            pem.setMouseClick(selectedElement.toString());
	            pem.setPathClick("Unknown");
	            SequentialEventData sed = new SequentialEventData("MouseClick Project Explorer", pem);
	            listSequntialevents.add(sed); 
		        System.out.println("Project Explorer > Unknown element selected of type: " + selectedElement.toString());
		    }
		} else {
		    System.out.println("Selection is not an instance of IStructuredSelection or is null.");
		}

		System.out.println();
	}

	private void removeSelectionListener(IWorkbenchWindow window) {
	    if (window == null) return;
	    Display display = window.getShell().getDisplay();
	    Runnable unregister = () -> {
	        try {
	            if (window.getShell() == null || window.getShell().isDisposed()) return;

	            // Only remove if we had added
	            if (!Boolean.TRUE.equals(window.getShell().getData(KEY_LISTENERS_ADDED))) return;

	            display.removeFilter(org.eclipse.swt.SWT.MouseDown,        mouseClickListener);
	            display.removeFilter(org.eclipse.swt.SWT.MouseDoubleClick, mouseClickListener);
	            display.removeFilter(org.eclipse.swt.SWT.KeyDown,          keyBoardClickListener);

	            window.getSelectionService().removePostSelectionListener(globalSelectionListener);

	            // If you keep a reference to the WindowClickListener, remove it here too.
	            // (You can store it in window.getShell().setData("EM_PART_LISTENER", listener) when adding.)
	            window.getShell().setData(KEY_LISTENERS_ADDED, Boolean.FALSE);
	        } catch (Throwable t) {
	            System.out.println("Exception happened here." + t.getMessage());
	        }
	    };

	    if (Display.getCurrent() == display) {
	        unregister.run();
	    } else {
	        display.asyncExec(unregister);
	    }
	}

	
	public void reset(IWorkbenchWindow window) {
		GlobalVars.listSequentialEvents.clear();
		listSequntialevents = new ArrayList<>();
	}

	@Override
	public void selectionChanged(IWorkbenchPart arg0, ISelection arg1) {
		// TODO Auto-generated method stub
		System.out.println("selection changed");
	}

}