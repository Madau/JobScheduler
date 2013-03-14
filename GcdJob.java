
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryProxy;
import java.math.BigInteger;
import java.rmi.RemoteException;

/**
 * Class GcdJob is a job that extends BaseJob
 * This class consists of two BigIntegers that we use to compute a gcd between
 * and a BigInteger that is the result of this gcd computation
 * This class has the ability to send a copy of itself to the job scheduler
 * Then, it waits for a response and outputs the gcd of the two input numbers
 *
 * @author Matt Au
 */
public class GcdJob extends BaseJob {
    
    // Hidden data members
    private BigInteger intOne;
    private BigInteger intTwo;
    private BigInteger myGCD;
    
    /**
     * Constructor taking two BigInteger inputs
     * 
     * @param intOne a BigInteger that we are interested in finding the 
     *               gcd of in regards to a second BigInteger
     * @param intTwo the second BigInteger that we are interested in
     */
    public GcdJob(BigInteger intOne, BigInteger intTwo) {
        
        this.intOne = intOne;
        this.intTwo = intTwo;
    }
    
    /**
     * This is the job computation
     * There is a 10 second sleep period along with a simple call to the 
     * BigInteger gcd function that changes the internal myGCD BigInteger
     */
    public void computeJob() {
        
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) { }
        
        myGCD = intOne.gcd(intTwo);
    }
    
    /**
     * Main function
     * Sets up the parameters to enable proper job execution
     * Ensures proper inputs for GcdJob
     * Attempts to set up communication with registry server and job scheduler
     * Waits for the computation to return and outputs the gcd of the object
     * 
     * @param args Command line input should be: 
     *             host port jobschedulername jobname BigInteger1 BigInteger2
     * @throws RemoteException Thrown if there is a remote error
     * @throws NotBoundException Thrown if job scheduler isn't bound
     */
    public static void main(String[] args) throws 
            RemoteException, NotBoundException {        
        
        // Invalid argument length
        if(args.length != 6) {
            
            throw new IllegalArgumentException("Proper execution: java "
                    + "GcdJob <host> <port> <jsname> <jobname> <x> <y>");
        }        
        
        String host = args[0];
        int port;
        
        BigInteger intOne;
        BigInteger intTwo;
        //Invalid port input
        try {
            port = Integer.parseInt (args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("GcdJob: Invalid port "
                    + "number: " + args[1]);
        }
        
        //Invalid BigInteger input
        try {
            intOne = new BigInteger(args[4]);
            intTwo = new BigInteger(args[5]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("GcdJob: Invalid integer(s) "
                    + "from input: " + args[4] + " " + args[5]);
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
        
        //Improper JobScheduler name
        try {
            jobSched = (JobSchedulerInterface) proxyBot.lookup(args[2]);            
        } catch (NotBoundException e) {
            throw new RemoteException ("ComputerServer(): Unable to find job "
                    + "scheduler " + args[2]);
        }
        
        GcdJob myJob = new GcdJob(intOne, intTwo);
        myJob.setType("GCD");
        myJob.setName(args[3]);
             
        myJob = (GcdJob) jobSched.doJob(myJob, false);
        
        System.out.println(myJob.myGCD);
    }
}
