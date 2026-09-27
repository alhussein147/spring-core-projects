# Library Management System

This project is a small JPA and H2 example that demonstrates associations, lifecycle ownership, fetching, inheritance, JPQL, and Criteria API queries.

## Domain mapping

- `Author` and `Book` form a bidirectional one-to-many/many-to-one relationship. `Book.author` is the owning side and writes the `author_id` foreign key. `Author.books` is the inverse side through `mappedBy = "author"`.
- `Author.books` uses `CascadeType.ALL` and `orphanRemoval = true`. Persisting an author persists newly added books, and removing a book through `removeBook` deletes the orphaned row. `addBook` keeps the Java object graph synchronized by updating both the collection and the book's author.
- `Book.publisher` is a lazy `@ManyToOne` with the `publisher_id` foreign key. Publisher lifecycle is intentionally independent from a book, so it is not cascaded.
- `Book.categories` is the owning side of a lazy many-to-many relationship. The `book_categories` join table stores `book_id` and `category_id`; `Category.books` is the inverse side.
- `LibraryUser` is the abstract root of the `Employee` and `Customer` hierarchy. The `JOINED` strategy keeps shared user fields in `LibraryUser` and subtype-specific fields in their own normalized tables. It avoids the nullable, subtype-only columns of a single-table design and works well when each subtype has meaningful distinct data.

Collections are explicitly lazy. The two `Book` to-one associations are also explicitly lazy, even though JPA defaults to eager for to-one mappings, so related entities are loaded only when the active persistence context needs them.

## Sample data and queries

`LibraryManagementDemo` inserts publishers, categories, authors, books, an employee, and a customer. `LibraryQueryService` contains the exercises:

- JPQL book lookup by author name and publisher name.
- JPQL lookup of a book id using positional parameter `?1`.
- `left join fetch` to load an author and their books in one query.
- `count` and `group by` projection into `AuthorBookCount`.
- Criteria title search and a dynamic Criteria query with optional title and author-name predicates.

`findAuthorByName` is the normal lazy query: its `books` collection remains uninitialized until accessed. If the author is detached before that access, Hibernate cannot load the collection and throws `LazyInitializationException`. `findAuthorWithBooksByName` uses `JOIN FETCH`, initializing the books in the same query, so the returned author can safely expose that collection after the entity manager closes for this use case.

The portable, standard JPQL form for a case-insensitive title lookup is `lower(b.title) = lower(:title)`, which is what the Criteria implementation expresses. Hibernate HQL also offers database-oriented extensions such as `ilike`, for example `from Book b where b.title ilike :title`. That feature is convenient for case-insensitive pattern matching but is not portable JPQL and can tie the query to Hibernate or a database dialect.

## Running manually

Use your preferred Maven or IDE configuration to run `com.example.lms.LibraryManagementDemo`. It creates the H2 in-memory schema, inserts sample data, runs the query examples, and prints the generated schema metadata.
