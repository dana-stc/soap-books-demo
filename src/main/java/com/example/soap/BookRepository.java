package com.example.soap;

import com.example.books.Book;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** A fake "database": books live in memory and vanish when the app stops. */
@Component
public class BookRepository {

    private final Map<Integer, Book> books = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public BookRepository() {
        add("Dune", "Frank Herbert", 1965);
        add("1984", "George Orwell", 1949);
        add("The Hobbit", "J.R.R. Tolkien", 1937);
    }

    public Optional<Book> findById(int id) {
        return Optional.ofNullable(books.get(id));
    }

    public Book add(String title, String author, int year) {
        Book book = new Book();
        book.setId(nextId.getAndIncrement());
        book.setTitle(title);
        book.setAuthor(author);
        book.setYear(year);
        books.put(book.getId(), book);
        return book;
    }
}
