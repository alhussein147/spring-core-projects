package com.example.lms;

import com.example.lms.model.Author;
import com.example.lms.model.Book;
import com.example.lms.model.Category;
import com.example.lms.model.Customer;
import com.example.lms.model.Employee;
import com.example.lms.model.Publisher;
import com.example.lms.query.LibraryQueryService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;

public final class LibraryManagementDemo {

    public static void main(String[] args) {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("libraryPU");
        try {
            SampleData sampleData = seedDatabase(factory);
            runQueryExamples(factory, sampleData);
        } finally {
            factory.close();
        }
    }

    private static SampleData seedDatabase(EntityManagerFactory factory) {
        EntityManager entityManager = factory.createEntityManager();
        try {
            entityManager.getTransaction().begin();

            Publisher addisonWesley = new Publisher("Addison-Wesley");
            Publisher harperCollins = new Publisher("HarperCollins");

            Category programming = new Category("Programming");
            Category classics = new Category("Classics");
            Category fiction = new Category("Fiction");

            entityManager.persist(addisonWesley);
            entityManager.persist(harperCollins);

            entityManager.persist(programming);
            entityManager.persist(classics);
            entityManager.persist(fiction);

            Author robertMartin = new Author("Robert C. Martin");
            Book cleanCode = new Book("Clean Code", addisonWesley);

            cleanCode.addCategory(programming);
            robertMartin.addBook(cleanCode);
            entityManager.persist(robertMartin);

            Author harperLee = new Author("Harper Lee");
            Book mockingbird = new Book("To Kill a Mockingbird", harperCollins);

            mockingbird.addCategory(classics);
            mockingbird.addCategory(fiction);

            harperLee.addBook(mockingbird);
            entityManager.persist(harperLee);

            entityManager.persist(new Employee("Maya Patel", "maya.patel@library.test", "Librarian"));
            entityManager.persist(new Customer("Daniel Reed", "daniel.reed@example.test", "MEM-1042"));

            entityManager.getTransaction().commit();
            System.out.println("Sample library data persisted.");
            return new SampleData(cleanCode.getId(), robertMartin.getName(), addisonWesley.getName());
        } catch (RuntimeException exception) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw exception;
        } finally {
            entityManager.close();
        }
    }

    private static void runQueryExamples(EntityManagerFactory factory, SampleData sampleData) {
        EntityManager entityManager = factory.createEntityManager();
        try {
            LibraryQueryService queries = new LibraryQueryService(entityManager);
            System.out.println("Books by author: " + queries.findBooksByAuthorName(sampleData.authorName()).size());
            System.out.println("Books by publisher: " + queries.findBooksByPublisherName(sampleData.publisherName()).size());
            System.out.println("Book found by positional id: " + queries.findBookById(sampleData.bookId()).isPresent());
            System.out.println("Author fetched with books: "
                    + queries.findAuthorWithBooksByName(sampleData.authorName()).map(author -> author.getBooks().size()).orElse(0));
            System.out.println("Author book totals: " + queries.countBooksByAuthor());
            System.out.println("Criteria title search: " + queries.findBooksByTitle("Clean Code").size());
            System.out.println("Criteria optional filters: " + queries.findBooks("Clean Code", sampleData.authorName()).size());
        } finally {
            entityManager.close();
        }
    }

    private record SampleData(Long bookId, String authorName, String publisherName) {
    }
}
