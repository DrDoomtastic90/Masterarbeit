package errorHandler;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class AuthenticationRequestException extends MappedCustomException implements ExceptionMapper<AuthenticationRequestException> {
    private static final long serialVersionUID = 1L;
 
    public AuthenticationRequestException() {
        super("Invalid Login Credentials");
    }
 
    public AuthenticationRequestException(String string) {
        super(string);
    }
 
    @Override
    public Response toResponse(AuthenticationRequestException exception)
    {
        return Response.status(404).entity(exception.getMessage())
                                    .type("text/plain").build();
    }
}
