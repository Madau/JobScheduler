
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryProxy;
import java.math.BigInteger;
import java.rmi.RemoteException;

/**
 * Class PrimalityJob is a job that extends BaseJob
 * This class consists of one BigIntegers that we need to determine is prime or 
 * not and a string that represents the primality as prime or composite
 * 
 * This class has the ability to send a copy of itself to the job scheduler
 * Then, it waits for a response and outputs the primality of the input number
 *
 * @author Matt Au
 */
public class PrimalityJob extends BaseJob {
    
    // Hidden data members
    private BigInteger intOne;
    private String primality;
    
    /**
     * Constructor taking two BigInteger inputs
     * 
     * @param intOne a BigInteger that we will determine primality of
     */
    public PrimalityJob(BigInteger intOne) {
        
        this.intOne = intOne;
    }
    
    /**
     * This is the job computation
     * There is a 10 second sleep period along with a simple call to the 
     * BigInteger isProbablePrime function that changes the internal primality
     * String
     */
    public void computeJob() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) { }
        
        if(intOne.isProbablePrime(64)) {
            
            primality = "prime";
        }
        
        else {
            
            primality = "composite";
        }
    }
    
    /**
     * Main function
     * Sets up the parameters to enable proper job execution
     * Ensures proper inputs for PrimalityJob
     * Attempts to set up communication with registry server and job scheduler
     * Waits for the computation to return and outputs the primality of the 
     * BigInteger
     * 
     * @param args Command line input should be: 
     *             host port jobschedulername jobname BigInteger1
     * @throws RemoteException Thrown if there is a remote error
     * @throws NotBoundException Thrown if job scheduler isn't bound
     */
    public static void main(String[] args) throws 
            RemoteException, NotBoundException {        
        
        //Improper argument length
        if(args.length != 5) {
            
            throw new IllegalArgumentException("Proper execution: java "
                    + "PrimalityJob <host> <port> <jsname> <jobname> <x>");
        }        
        
        String host = args[0];
        int port;        
        BigInteger intOne;
        
        // Improper port input
        try {
            port = Integer.parseInt (args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("PrimalityJob: Invalid port "
                    + "number: " + args[1]);
        }
        
        // Improper BigInteger input
        try {
            intOne = new BigInteger(args[4]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("PrimalityJob: Invalid integer(s) "
                    + "from input: " + args[4]);
        }
        
        RegistryProxy proxyBot = null;
        //Unreachable host
        try {
            proxyBot = new RegistryProxy(host, port);
        } catch (RemoteException e) {
            throw new RemoteException("Host unreachable or "
                    + "invalid host name/port");
        }
        
        JobSchedulerInterface jobSched;
        
        //Wrong JobScheduler name
        try {
            jobSched = (JobSchedulerInterface) proxyBot.lookup(args[2]);            
        } catch (NotBoundException e) {
            throw new RemoteException ("ComputerServer(): Unable to find job "
                    + "scheduler " + args[2]);
        }
        
        PrimalityJob myJob = new PrimalityJob(intOne);
        myJob.setType("Primality");
        myJob.setName(args[3]);
             
        myJob = (PrimalityJob) jobSched.doJob(myJob, false);
        
        System.out.println(myJob.primality);
    }
}
