import edu.rit.ds.registry.NotBoundException;
import java.rmi.RemoteException;
import edu.rit.ds.registry.RegistryProxy;
import java.rmi.server.UnicastRemoteObject;

/**
 * Distributed object ComputeServer performs a run function on a job and
 * returns the job to the caller
 * 
 * @author Matt Au
 */
public class ComputeServer implements ComputeServerInterface{
    
    //Hidden data members
    private JobSchedulerInterface jobSched;
    private String name;
    
    /**
     * ComputeJob function is to simply call the computeJob function on a job
     * 
     * @param job BaseJob object that will have a job run
     * @return BaseJob object after completion of job
     * @throws RemoteException thrown if there are any remote issues
     */
    @Override
    public BaseJob computeJob(BaseJob job) throws RemoteException {
        
        job.computeJob();
        return job;
    }
    
    /**
     * Constructor for Start program
     * 
     * @param args String[] arguments used for object construction
     *             Params are host port jobschedulername servername
     * @throws RemoteException thrown if there are any issues connecting to the
     *                         registry or JobScheduler
     */
    public ComputeServer(String[] args) throws RemoteException {
       
        super();
        
        //Invalid amount of arguments
        if(args.length != 4) {
            
            throw new IllegalArgumentException("Proper execution: java Start "
                    + "ComputeServer <host> <port> <jsname> <csname>");
        }
        
        String host = args[0];
        name = args[3];
        int port;
        
        //Invalid port input
        try {
            port = Integer.parseInt (args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ComputeServer: Invalid port "
                    + "number: " + args[1]);
        }
        
        RegistryProxy proxyBot = null;
        
        //Invalid host name/port
        try {
            proxyBot = new RegistryProxy(host, port);
        } catch (RemoteException e) {
            throw new RemoteException("Host unreachable or "
                    + "invalid host name/port");
        }
        
        ComputeServerInterface remoteReference = 
                (ComputeServerInterface)UnicastRemoteObject.
                exportObject(this, 0);
        
        //Unable to reach JobScheduler, or it is unbound
        //Or unable to register
        try {
            jobSched = (JobSchedulerInterface) proxyBot.lookup(args[2]);  
            jobSched.registerServer(remoteReference);
        } catch (RemoteException e ) {
            throw new RemoteException ("ComputeServer(): Unable to find job "
                    + "scheduler " + args[2]);
        }
        catch (NotBoundException e) {
            throw new RemoteException ("ComputeServer(): Unable to find job "
                    + "scheduler " + args[2]);
        }
         
    }
    
    /**
     * Function used to get the name of the server
     * 
     * @return String that is the name of the server
     * @throws RemoteException thrown if there are any errors connecting
     */
    @Override
    public String getName() throws RemoteException {
        return name;
    }
}
