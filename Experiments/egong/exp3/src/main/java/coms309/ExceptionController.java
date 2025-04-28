package coms309;

/**
 * Controller used to showcase customizing the output when an exception is thrown
 *
 * @author Eddie M.
 */

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// An attempt to handle Exceptions under Spring Boot, with message from the handler and exception
@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleException(RuntimeException e){
        Map<String, Object> message = new HashMap<>();
        message.put("Maybe another day, my guy", e.getMessage());

        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }
}
