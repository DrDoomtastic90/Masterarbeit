package errorHandler;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class CustomException extends MappedCustomException implements ExceptionMapper<CustomException> {
    private static final long serialVersionUID = 1L;
 
    public CustomException() {
        super("An Error occured");
    }
 
    public CustomException(String string) {
        super(string);
    }
 
    @Override
    public Response toResponse(CustomException exception)
    {
        return Response.status(404).entity(exception.getMessage())
                                    .type("text/plain").build();
    }
}