package com.ge.snowizard.application.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ge.snowizard.api.SnowizardError;
import com.ge.snowizard.application.core.MediaTypeAdditional;

public class SnowizardExceptionMapper implements
ExceptionMapper<SnowizardException> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SnowizardExceptionMapper.class);

    @Override
    public Response toResponse(final SnowizardException exception) {
        LOGGER.debug("Error response ({}): {}", exception.getCode(),
                exception.getMessage());

        return Response
                .status(exception.getStatus())
                .entity(new SnowizardError(exception.getCode(), exception
                        .getMessage()))
                        .type(MediaTypeAdditional.APPLICATION_JSON_UTF8).build();
    }
}
