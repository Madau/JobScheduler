import java.io.Serializable;
import java.util.UUID;
/**
 * Abstract class BaseJob is meant to be a base for the two job types
 * It comes with a set of mutators and accessors for common variables
 * It also has a single abstract function that allows computation of jobs
 * 
 * @author Matt Au
 */
public abstract class BaseJob implements Serializable{
    
    // Hidden data members
    private String type = null;
    private String name = null;
    private UUID id = null;
    
    /**
     * Empty constructor
     */
    public BaseJob() { }
    
    /**
     * Mutator for type of job
     * 
     * @param type the type of job that the implementation is 
     */
    public void setType(String type) {
        
        this.type = type;
    }
    
    /**
     * Accessor for type
     * 
     * @return the type of job
     */
    public String getType() {
        
        return type;
    }
    
    /**
     * Mutator for name of job
     * 
     * @param name the name of job 
     */
    public void setName(String name) {
        
        this.name = name;
    }
    
    /**
     * Accessor for name
     * 
     * @return the name of the job
     */
    public String getName() {
        
        return name;
    }
    
    /**
     * Mutator for id
     * 
     * @param id unique identifier for this job
     */
    public void setID(UUID id) {
        
        this.id = id;
    }
    
    /**
     * Accessor for id
     * 
     * @return the unique id of the job
     */
    public UUID getID() {
        
        return id;
    }
    
    /**
     * The job computation is implemented separately for prime and GCD
     */
    public abstract void computeJob();
}
