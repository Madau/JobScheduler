import edu.rit.ds.Lease;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryProxy;
import edu.rit.ds.RemoteEventGenerator;
import edu.rit.ds.RemoteEventListener;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * JobScheduler implementation
 * 
 * The JobScheduler object handles all job requests and sends messages to all
 * registered Logger objects
 * 
 * Jobs are inserted into a queue awaiting a vacant ComputeServer object 
 * to allow computation to occur
 * 
 * The JobScheduler object handles registration of ComputeServers objects 
 * and Logger objects
 * 
 * @author Matt Au
 */
public class JobScheduler implements JobSchedulerInterface {
    
    //Hidden data members
    private RegistryProxy proxyBot;
    private String myName;
    private RemoteEventGenerator<JobSchedulerEvent> eventGenerator;
    private static LinkedBlockingQueue<BaseJob> requestQueue = 
            new LinkedBlockingQueue<BaseJob>();
    private ArrayList<ComputeServerInterface> availServerList = 
            new ArrayList<ComputeServerInterface>();
       
    /**
     * JobScheduler constructor for Start
     * 
     * @param args String[] arguments are in the format below
     *             host port JobSchedulername
     * @throws java.rmi.RemoteException 
     */
    public JobScheduler(String[] args) throws java.rmi.RemoteException {
        super();
        
        // improper number of arguments
        if(args.length != 3) {
            
            throw new IllegalArgumentException("Proper execution: java Start "
                    + "JobScheduler <host> <port> <jsname>");
        }
        
        String host = args[0];
        int port;
        
        //Invalid port number
        try {
            port = Integer.parseInt (args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("JobScheduler: Invalid port "
                    + "number: " + args[1]);
        }
        
        //Invalid or unreachable host
        try {
            proxyBot = new RegistryProxy(host, port);
        } catch (RemoteException e) {
            throw new RemoteException("Host unreachable or "
                    + "invalid host name/port");
        }
        
        JobSchedulerInterface remoteReference = 
                (JobSchedulerInterface)UnicastRemoteObject.
                exportObject(this, 0);
        
        //Attempt to bind in registry
        try {
            // Same name exists already
            if ( proxyBot.list().contains(args[2]) ) {
                
                throw new IllegalArgumentException("JobScheduler: "
                        + "Name already in existence: "+ args[2]);
            }
            else {
                
                proxyBot.rebind(args[2], this);
                myName = args[2];
            }
        } catch (RemoteException e) {
            try {
                UnicastRemoteObject.unexportObject(this, true);
            } catch (NoSuchObjectException e2) { }
            
            throw new RemoteException("Unable to unexport self");
        }
        
        eventGenerator = new RemoteEventGenerator<JobSchedulerEvent>();
    }   
    
    /**
     * Removes disconnected ComputeServer objects from the available server list
     * 
     * @param removeMe a list of ComputeServerInterface objects that need to be
     *                 removed from the server list
     */
    private void pruneServers(ArrayList<ComputeServerInterface> removeMe) {
                
        for( int j = 0; j < removeMe.size(); j++ ) {

            availServerList.remove(removeMe.get(j));
        }  
    }
    
    /**
     * Function to retrieve an available ComputeServer to perform a job
     * 
     * @return a ComputeServerInterface that represents an available server
     */
    private ComputeServerInterface getServer(){
                       
        synchronized(this) {
            
            ArrayList<ComputeServerInterface> removeMe = 
                new ArrayList<ComputeServerInterface>();
            
            for(int i = 0; i < availServerList.size(); i++ ) {
                ComputeServerInterface server = availServerList.get(i);
                try{
                    server.getName(); // Determine if alive                
                    //Found valid server, remove bad ones found
                    if(removeMe.size() > 0) {
                                
                        pruneServers(removeMe);
                    }
                    return server;
                    
                } catch ( RemoteException e) {
                    removeMe.add(server); // Add bad server to a list to remove
                }
            }
            //No available servers, prune list
            if(removeMe.size() > 0) {
                            
                pruneServers(removeMe);
            }
        }
        
        // No available servers, wait 5 seconds, try again
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) { }
        
        return getServer();
    }
    
    /**
     * Function that is called remotely by BaseJob classes to sent a job to the
     * JobScheduler
     * 
     * Adds BaseJob to a queue and waits for the object to reappear at the top
     * of the queue.  Once this happens, the JobScheduler attempts to find an
     * available ComputeServer.  After this, the BaseJob object is sent to the
     * ComputeServer.  After getting an answer, the BaseJob is returned to the
     * job that initially called it.
     * 
     * If an error occurs during computation, the job is resent to the queue
     * 
     * This function also sends messages to all known Logger objects whenever a
     * job is added the queue, sent to a server, or finished by a server.
     * 
     * @param job a BaseJob that needs to have its computation
     * @param reDo a boolean value that is true only on the first queue of a
     *             BaseJob.  If a job is dropped and requeued, this value is
     *             false.
     * @return a BaseJob that has had its computation run to completion
     * @throws NotBoundException thrown 
     */
    @Override
    public BaseJob doJob(BaseJob job, boolean reDo) 
            throws RemoteException,NotBoundException{      
        
        //Set unique ID for job
        job.setID(UUID.randomUUID());
        requestQueue.offer(job);
        
        //If first time in the queue
        if( !reDo ) {
            
            //Send event
            eventGenerator.reportEvent( new JobSchedulerEvent("Job " + 
                    job.getName() + " scheduled"));
        }
        
        // While the top of the queue isn't our uniquely ID'd job
        while(!requestQueue.peek().getID().equals(job.getID())) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) { }
        }        
        
        //Get a server
        ComputeServerInterface server = getServer();
        availServerList.remove(server);
        
        requestQueue.poll();
        BaseJob returnJob = null;
        String serverName = null;
        
        //Send job object to ComputeServer object and send event
        try {
            serverName = server.getName();
            eventGenerator.reportEvent( new JobSchedulerEvent("Job " + 
                    job.getName() + " started on " + serverName));
            returnJob = server.computeJob(job);
        } catch (RemoteException e) {

            return doJob(job, true);
        }
            
        //Send message, set server availibility and return the job object
        eventGenerator.reportEvent( new JobSchedulerEvent("Job " + 
                job.getName() + " finished on " + serverName));
        availServerList.add(server);
        return returnJob;
    }

    /**
     * Allows registration of a ComputeServer object with the 
     * JobScheduler object
     * 
     * @param serverRef a ComputeServerInterface object that is a reference to a
     *                  ComputeServer object running remotely elsewhere
     * @return true if the service is performed
     * @throws RemoteException if there is a problem contacting the JobScheduler
     */
    @Override
    public boolean registerServer(ComputeServerInterface serverRef) 
            throws RemoteException {
        
        synchronized(this) {
            
            availServerList.add(serverRef);
            return true;
        }      
    }

    /**
     * Allows registration of a RemoteEventListner object from Logger with the 
     * JobScheduler object
     * 
     * @param listener a RemoteEventListener object that will need to be
     *                 broadcast to when certain events happen
     * @return a Lease object to the calling Object
     * @throws RemoteException if there is a problem contacting the JobScheduler
     */
    @Override
    public Lease registerLogger(RemoteEventListener<JobSchedulerEvent> listener) 
            throws RemoteException {
        
        return eventGenerator.addListener(listener);
    }
}
