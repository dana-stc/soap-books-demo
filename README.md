# SOAP Books Demo (Spring Boot) – a beginner's guide

A tiny SOAP web service: you can **look up a book by id** and **add a new book**.
Books are kept in memory (no database), so the code stays focused on SOAP.

## 1. What is SOAP?

SOAP (*Simple Object Access Protocol*) is a way for two programs to talk over the network
by exchanging **XML messages**, usually over HTTP.

Compare with REST, which you may know already:

| | REST | SOAP |
|---|---|---|
| Message format | usually JSON | always XML |
| What defines the API | URLs + HTTP verbs (GET/POST…) | a **WSDL** contract file |
| URLs | many (`/books/1`) | typically **one** (`/ws`); the XML body says what you want |
| Errors | HTTP status codes (404…) | a `<Fault>` element inside the XML |
| Typical use | public/mobile/web APIs | banks, insurance, government, older enterprise systems |

### A SOAP message is an "envelope"

```xml
<soapenv:Envelope ...>      <!-- the envelope: wraps everything -->
  <soapenv:Header/>         <!-- optional: extras like auth tokens -->
  <soapenv:Body>            <!-- the actual request or response -->
    <b:getBookRequest>
      <b:id>1</b:id>
    </b:getBookRequest>
  </soapenv:Body>
</soapenv:Envelope>
```

### The three key words

- **XSD** (XML Schema) – describes the *shape of the data*: "a book has an id, title, author, year".
- **WSDL** (Web Services Description Language) – the *contract*: "this service offers these operations,
  send messages shaped like this, at this address". Clients read it to know how to call you
  (many tools can even generate client code from it).
- **Endpoint** – the Java class that receives the request and returns the response.

## 2. How this project works (contract-first)

We write the XSD first; everything else follows from it.

```
books.xsd ──(build time)──> generated Java classes: GetBookRequest, GetBookResponse, Book, ...
    │
    └──(run time)──> Spring builds the WSDL for you at /ws/books.wsdl
```

Request flow:

```
Client ──XML over HTTP POST /ws──> MessageDispatcherServlet
                                        │ looks at the root element of <Body>
                                        ▼
                                   BookEndpoint.getBook(...)  ──> BookRepository
                                        │
Client <────── XML response ────────────┘
```

## 3. The files, one by one

| File | What it does |
|---|---|
| `src/main/resources/books.xsd` | **The contract.** Defines the messages (`getBookRequest`, `getBookResponse`, `addBookRequest`, `addBookResponse`) and the `book` type. |
| `pom.xml` | Dependencies (`spring-boot-starter-web-services`, `wsdl4j`, JAXB) and the **jaxb-maven-plugin** that turns the XSD into Java classes during the build. |
| `WebServiceConfig.java` | Turns SOAP on (`@EnableWs`), routes `/ws/*` to Spring-WS, and publishes the WSDL generated from the XSD. |
| `BookEndpoint.java` | **Your logic.** `@Endpoint` = "I handle SOAP messages". `@PayloadRoot(...)` = "call this method when the body's root element is `getBookRequest`". Think of it as `@RestController` + `@GetMapping`. |
| `BookRepository.java` | In-memory storage with 3 sample books. |
| `SoapBooksApplication.java` | The usual Spring Boot `main`. |
| `requests/*.xml` | Ready-made sample SOAP requests. |
| `BookEndpointTest.java` | Starts the app and checks a SOAP call and the WSDL work. |

Note: the classes `GetBookRequest`, `Book`, etc. do **not** exist in `src/`. They are generated into
`target/generated-sources/xjc` when you build. Don't edit them – change the XSD instead.

## 4. Run it

Requires Java 17+. Maven is included via the wrapper (`mvnw`).

```bash
./mvnw spring-boot:run
```

### See the WSDL (the contract)

Open <http://localhost:8080/ws/books.wsdl> in your browser.

### Call the service

Get book #1:

```bash
curl -s -H "Content-Type: text/xml" -d @requests/get-book.xml http://localhost:8080/ws
```

Response:

```xml
<SOAP-ENV:Envelope ...><SOAP-ENV:Header/><SOAP-ENV:Body>
  <ns2:getBookResponse xmlns:ns2="http://example.com/books">
    <ns2:book><ns2:id>1</ns2:id><ns2:title>Dune</ns2:title>
      <ns2:author>Frank Herbert</ns2:author><ns2:year>1965</ns2:year></ns2:book>
  </ns2:getBookResponse>
</SOAP-ENV:Body></SOAP-ENV:Envelope>
```

Add a book:

```bash
curl -s -H "Content-Type: text/xml" -d @requests/add-book.xml http://localhost:8080/ws
```

Try an unknown id (edit `<b:id>` to `99`) and you get a SOAP **Fault** with
`No book with id 99`.

You can also use a GUI tool such as **SoapUI** or **Postman**: import
`http://localhost:8080/ws/books.wsdl` and it builds the requests for you.

### Run the tests

```bash
./mvnw test
```

## 5. Try changing it

1. Add a `genre` element to `book` in `books.xsd`, rebuild, and set it in `BookRepository`.
2. Add a new operation `deleteBookRequest` / `deleteBookResponse`: add to the XSD, then add a method
   with `@PayloadRoot` in `BookEndpoint`.
3. Open the WSDL again and notice your new operation appeared automatically.

## 6. Common gotchas

- **Namespace must match.** `http://example.com/books` appears in the XSD, `WebServiceConfig` and
  `BookEndpoint`. If a request says nothing like "No endpoint mapped", the namespace or element name differs.
- **Content-Type** must be `text/xml` (SOAP 1.1).
- **Unknown classes in the IDE?** Run `./mvnw compile` once so the generated classes exist, and mark
  `target/generated-sources/xjc` as a source folder if needed.
