//https://medium.com/@ozerturk/exception-handling-with-rest-webservices-using-controlleradvice-annotation-7fe9afb4a68c
//brukt i tidligere eksamen PGR209

package com.bookingapp.cabin.backend.globalException;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}