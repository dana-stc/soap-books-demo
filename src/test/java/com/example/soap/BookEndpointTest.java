package com.example.soap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookEndpointTest {

    @LocalServerPort
    int port;

    private String post(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        return new RestTemplate().postForObject("http://localhost:" + port + "/ws",
                new HttpEntity<>(body, headers), String.class);
    }

    @Test
    void getBookReturnsDune() {
        String xml = post("""
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:b="http://example.com/books">
                <soapenv:Body><b:getBookRequest><b:id>1</b:id></b:getBookRequest></soapenv:Body>
                </soapenv:Envelope>""");
        assertTrue(xml.contains("Dune"), xml);
    }

    @Test
    void wsdlIsPublished() {
        String wsdl = new RestTemplate().getForObject("http://localhost:" + port + "/ws/books.wsdl", String.class);
        assertTrue(wsdl.contains("BooksPort"));
    }
}
