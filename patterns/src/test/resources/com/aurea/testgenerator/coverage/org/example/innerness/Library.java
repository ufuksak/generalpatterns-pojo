package org.example.innerness;

import java.util.Collections;
import java.util.List;

public class Library {

    public static class Shelf {
        public static class Book {
            public String name;

            public Book(String name) {
                this.name = name;
            }
        }

        public List<Book> repair(Librarian librarian) {
            return Collections.emptyList();
        }

        public Librarian getAdministrator() {
            return new Librarian();
        }
    }

    public static class Librarian {
    }

    Shelf.Book getBookFromTopShelf(Librarian librarian) {
        return new Shelf.Book("Wikipedia");
    }
}
