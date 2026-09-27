package com.example.lms.query;

import com.example.lms.model.Author;
import com.example.lms.model.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class LibraryQueryService {

    private final EntityManager entityManager;

    public LibraryQueryService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Book> findBooksByAuthorName(String authorName) {
        return entityManager.createQuery("""
                select b
                from Book b
                where b.author.name = :authorName
                order by b.title
                """, Book.class)
                .setParameter("authorName", authorName)
                .getResultList();
    }

    public List<Book> findBooksByPublisherName(String publisherName) {
        return entityManager.createQuery("""
                select b
                from Book b
                where b.publisher.name = :publisherName
                order by b.title
                """, Book.class)
                .setParameter("publisherName", publisherName)
                .getResultList();
    }

    public Optional<Book> findBookById(Long bookId) {
        return entityManager.createQuery("select b from Book b where b.id = ?1", Book.class)
                .setParameter(1, bookId)
                .getResultStream()
                .findFirst();
    }

    public Optional<Author> findAuthorByName(String authorName) {
        return entityManager.createQuery("select a from Author a where a.name = :authorName", Author.class)
                .setParameter("authorName", authorName)
                .getResultStream()
                .findFirst();
    }

    public Optional<Author> findAuthorWithBooksByName(String authorName) {
        return entityManager.createQuery("""
                select distinct a
                from Author a
                left join fetch a.books
                where a.name = :authorName
                """, Author.class)
                .setParameter("authorName", authorName)
                .getResultStream()
                .findFirst();
    }

    public List<AuthorBookCount> countBooksByAuthor() {
        return entityManager.createQuery("""
                select new com.example.lms.query.AuthorBookCount(a.name, count(b))
                from Author a
                left join a.books b
                group by a.id, a.name
                order by a.name
                """, AuthorBookCount.class)
                .getResultList();
    }

    public List<Book> findBooksByTitle(String title) {
        return findBooks(title, null);
    }

    public List<Book> findBooks(String title, String authorName) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Book> query = builder.createQuery(Book.class);
        Root<Book> book = query.from(Book.class);
        List<Predicate> predicates = new ArrayList<>();

        if (hasText(title)) {
            predicates.add(builder.equal(builder.lower(book.get("title")), title.toLowerCase()));
        }
        if (hasText(authorName)) {
            Join<Book, Author> author = book.join("author");
            predicates.add(builder.equal(builder.lower(author.get("name")), authorName.toLowerCase()));
        }

        query.select(book).where(predicates.toArray(Predicate[]::new)).orderBy(builder.asc(book.get("title")));
        return entityManager.createQuery(query).getResultList();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
