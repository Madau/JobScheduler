import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryProxy;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Class Logger provides a client that simply receives messages from the Job
 * Scheduler
 * 
 * This class attempts to connect to the job scheduler and waits for
 * incoming messages
 *
 * @author Matt Au
 */
public class Logger{
    
    // Hidden variables
    private JobSchedulerInterface jobSched;
    private RegistryProxy proxyBot;
    
    /**
     * Constructor taking a String[] as arguments
     * 
     * @param args is a String[] containing 3 parameters
     *             host port jobschedulername
     * @throws RemoteException thrown if host is unreachable or if job scheduler
     *                         cannot be found
     */
    public Logger(String[] args) throws RemoteException {      
        
        // Invalid number of arguments
        if(args.length != 3) {
            
            throw new IllegalArgumentException("Proper execution: java "
                    + "Logger <host> <port> <jsname>");
        }
        
        String host = args[0];
        int port;
        
        // Invalid port number
        try {
            port = Integer.parseInt (args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Logger: Invalid port "
                    + "number: " + args[1]);
        }
        
        RegistryProxy proxyBot = null;
        
        //Invalid host or port number
        try {
            proxyBot = new RegistryProxy(host, port);
        } catch (RemoteException e) {
            throw new RemoteException("Host unreachable or "
                    + "invalid host name/port");
        }
        
       
        
        //Attempting to lookup job scheduler
        try {
            jobSched = (JobSchedulerInterface) proxyBot.lookup(args[2]);
            
        } catch (NotBoundException e) {
            throw new RemoteException ("Logger(): Unable to find job "
                    + "scheduler " + args[2]);
        }
    }
    
    /**
     * Main function
     * Attempts to set up logger and a RemoteEventListener
     * If a fail occurs during setup the program exits
     * Otherwise it sits idly
     * 
     * @param args is a String[] containing 3 parameters
     *             host port jobschedulername
     */
    public static void main(String[] args) {
        try {
            Logger myLogger = new Logger(args);
            RemoteEventListener<JobSchedulerEvent> listener = 
                new RemoteEventListener<JobSchedulerEvent>() {

            @Override
            public void report(long l, JobSchedulerEvent re) 
                    throws RemoteException {
                
                System.out.println(re.message);
            }
        };
        
        UnicastRemoteObject.exportObject (listener, 0);
        myLogger.jobSched.registerLogger(listener);
        
        } catch (Exception e) {
            
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}
