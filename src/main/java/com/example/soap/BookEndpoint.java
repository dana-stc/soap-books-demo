package com.example.soap;

import com.example.books.AddBookRequest;
import com.example.books.AddBookResponse;
import com.example.books.GetBookRequest;
import com.example.books.GetBookResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/** The SOAP equivalent of a @RestController. */
@Endpoint
public class BookEndpoint {

    private static final String NAMESPACE = "http://example.com/books";

    private final BookRepository repository;

    public BookEndpoint(BookRepository repository) {
        this.repository = repository;
    }

    // Runs when the XML body's root element is <getBookRequest> in our namespace.
    @PayloadRoot(namespace = NAMESPACE, localPart = "getBookRequest")
    @ResponsePayload
    public GetBookResponse getBook(@RequestPayload GetBookRequest request) {
        GetBookResponse response = new GetBookResponse();
        response.setBook(repository.findById(request.getId())
                .orElseThrow(() -> new BookNotFoundException("No book with id " + request.getId())));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "addBookRequest")
    @ResponsePayload
    public AddBookResponse addBook(@RequestPayload AddBookRequest request) {
        AddBookResponse response = new AddBookResponse();
        response.setBook(repository.add(request.getTitle(), request.getAuthor(), request.getYear()));
        return response;
    }

    /** Turned into a SOAP <Fault> (SOAP's version of an HTTP error). */
    @SoapFault(faultCode = FaultCode.CLIENT)
    public static class BookNotFoundException extends RuntimeException {
        public BookNotFoundException(String message) {
            super(message);
        }
    }
}
