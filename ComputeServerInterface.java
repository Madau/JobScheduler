import java.rmi.RemoteException;
import java.rmi.Remote;

/**
 * Interface for calling methods on ComputeServer objects through Java RMI
 * 
 * @author Matt Au
 */
public interface ComputeServerInterface extends Remote{
    
    /**
     * Makes a method call on a remote ComputeServer object to execute the 
     * computeJob function on a job
     * 
     * @param job the job to run the function on
     * @return a BaseJob that is the same as the one that came in, but with the
     *         computation run, thus changing some element of the object
     * @throws RemoteException throws an exception if an error occurs when
     *                         computing the job or contacting the server
     */
    BaseJob computeJob(BaseJob job) throws RemoteException;
    
    /**
     * Makes a method call on a remote ComputeServer object to return the name
     * of the server
     * 
     * @return a String that is the name of the server
     * @throws RemoteException throws an exception if an error occurs when
     *                         contacting the server
     */
    String getName() throws RemoteException;
}
