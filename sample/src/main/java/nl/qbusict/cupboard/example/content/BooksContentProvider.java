package nl.qbusict.cupboard.example.content;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import nl.qbusict.cupboard.example.model.Author;
import nl.qbusict.cupboard.example.model.Book;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class BooksContentProvider extends ContentProvider {

    public static String AUTHORITY = "nl.qbusict.cupboard.example.provider";
    public static final Uri AUTHOR_URI = Uri.parse("content://"+AUTHORITY+"/author");
    public static final Uri BOOKS_URI = Uri.parse("content://"+AUTHORITY+"/book");

    private SampleSQLiteOpenHelper mDatabaseHelper;
    private static UriMatcher sMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int BOOK = 0;
    private static final int BOOKS = 1;
    private static final int AUTHOR = 2;

    static {
        sMatcher.addURI(AUTHORITY, "book/#", BOOK);
        sMatcher.addURI(AUTHORITY, "book", BOOKS);
        sMatcher.addURI(AUTHORITY, "author/#", AUTHOR);
    }

    @Override
    public int delete(Uri uri, String selection, String[] args) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new SampleSQLiteOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        switch (sMatcher.match(uri)) {
        case BOOKS:
            // this is the full query syntax, most of the time you can leave out projection etc
            // if the content provider returns a fixed set of data
            return cupboard().withDatabase(db).query(Book.class).
                                               withProjection(projection).
                                               withSelection(selection, selectionArgs).
                                               orderBy(sortOrder).
                                               getCursor();
        case BOOK:
            return cupboard().withDatabase(db).query(Book.class).
                                               byId(ContentUris.parseId(uri)).
                                               getCursor();
        case AUTHOR:
            return cupboard().withDatabase(db).query(Author.class).
                                               byId(ContentUris.parseId(uri)).
                                               getCursor();
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

}
