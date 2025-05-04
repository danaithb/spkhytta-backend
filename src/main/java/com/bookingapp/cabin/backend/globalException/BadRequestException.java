//https://medium.com/@ozerturk/exception-handling-with-rest-webservices-using-controlleradvice-annotation-7fe9afb4a68c

package com.bookingapp.cabin.backend.globalException;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
