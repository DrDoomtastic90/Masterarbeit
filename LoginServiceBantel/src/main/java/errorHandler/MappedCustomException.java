package errorHandler;


public abstract class MappedCustomException extends Error {
    private static final long serialVersionUID = 1782950737480687456L;    
    
    public MappedCustomException() {
        super("The required Object could not be found ");
    }
 
    public MappedCustomException(String string) {
        super(string);
    }
}
