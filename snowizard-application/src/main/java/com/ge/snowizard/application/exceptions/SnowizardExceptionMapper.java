package com.ge.snowizard.application.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.dropwizard.jersey.errors.ErrorMessage;

@Provider
public class SnowizardExceptionMapper
        implements ExceptionMapper<SnowizardException> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SnowizardExceptionMapper.class);

    @Override
    public Response toResponse(final SnowizardException exception) {
        LOGGER.debug("Error response ({}): {}", exception.getCode(),
                exception.getMessage());

        return Response.status(exception.getStatus())
                .entity(new ErrorMessage(exception.getCode(),
                        exception.getMessage()))
                .type(MediaType.APPLICATION_JSON).build();
    }
}
