
import edu.rit.ds.Lease;
import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.NotBoundException;
import java.rmi.RemoteException;

/**
 * Interface defining how objects interact with a remote JobScheduler object
 * through Java RMI
 * 
 * @author Matt Au
 */
public interface JobSchedulerInterface extends java.rmi.Remote {
    
    /**
     * Function to send a job to the JobScheduler remotely.     * 
     * 
     * @param job is a job of any type to perform a job on, these are sent to
     *        servers
     * @param reDo a boolean parameter to prevent reposting messages to loggers
     *             when a job is rescheduled
     * @return returns a copy of the job after completion
     * @throws RemoteException thrown if there is a problem during doJob or
     *                         contacting the JobScheduler
     * @throws NotBoundException thrown if the JobScheduler is no longer bound
     */
    BaseJob doJob(BaseJob job, boolean reDo) throws RemoteException, NotBoundException;
    
    /**
     * Function to send a reference of a ComputeServer  object to the 
     * JobScheduler
     * 
     * Performed when a ComputeServer comes online
     * 
     * @param serverRef a ComputeServerInterface object that is a reference
     *                  to the ComputerServer that calls the function
     * @return true if a connection is made
     * @throws RemoteException thrown if an error occurs attempting to register
     *                         most likely the server is down
     */
    boolean registerServer(ComputeServerInterface serverRef) throws RemoteException;
        
    /**
     * Function to send a RemoteEventListener to the JobScheduler
     * Performed when a Logger comes online 
     * 
     * @param listener a RemoteEventListener object that will need to be
     *                 broadcast to when certain events happen
     * @return a Lease object
     * @throws RemoteException thrown if an error occurs attempting to contact
     *                         the JobScheduler object
     */
    Lease registerLogger(RemoteEventListener<JobSchedulerEvent> listener) 
            throws RemoteException;
}
