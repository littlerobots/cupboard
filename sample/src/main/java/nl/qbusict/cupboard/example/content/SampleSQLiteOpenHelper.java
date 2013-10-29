package nl.qbusict.cupboard.example.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import nl.qbusict.cupboard.example.model.Author;
import nl.qbusict.cupboard.example.model.Book;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class SampleSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "books.db";
    private static final int VERSION = 1;

    static {
        // register the models with cupboard. A model should be registered before it can be
        // used in any way. There are a few options to make sure the models are registered:
        // 1. In a static block like this in a SQLiteOpenHelper or ContentProvider
        // 2. In a custom Application class either form a static block or onCreate
        // 3. By creating your own factory class and have the static block there.

        cupboard().register(Book.class);
        cupboard().register(Author.class);
    }

    public SampleSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create tables won't upgrade tables, unlike upgradeTables() below.
        cupboard().withDatabase(db).createTables();
        // we can setup an index on the author field or add other tables if we like.
        db.execSQL("create index author_id on "+cupboard().getTable(Book.class)+" (author)");

        // setup some sample data
        Author shakespeare = new Author();
        shakespeare.name = "William Shakespeare";

        cupboard().withDatabase(db).put(shakespeare);

        Book romeoJulliet = new Book();
        romeoJulliet.title = "Romeo and Julliet";
        // the author should have an id assigned, since only the id will be stored on Book
        romeoJulliet.author = shakespeare;

        cupboard().withDatabase(db).put(romeoJulliet);

        // maybe we should do this in a transaction...OK!
        Author acd = new Author();
        db.beginTransaction();
        try {
            acd.name = "Arthur Conan Doyle";
            cupboard().withDatabase(db).put(acd);
            Book book = new Book();
            book.author = acd;
            book.title = "A Study in Scarlet";
            cupboard().withDatabase(db).put(book);
            book = new Book();
            book.author = acd;
            book.title = "The Sign of the Four";
            cupboard().withDatabase(db).put(book);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        // actually you can just put multiple entities in one transaction
        Book book1 = new Book();
        Book book2 = new Book();
        book1.author = acd;
        book1.title = "The Valley of Fear";
        book2.author = shakespeare;
        book2.title = "Othello";

        // put two books in a single transaction, mixing authors and books would work too, as long as the
        // author is put before the books he has written.
        cupboard().withDatabase(db).put(book1, book2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        cupboard().withDatabase(db).upgradeTables();
    }

}