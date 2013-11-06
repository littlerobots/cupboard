package nl.qbusict.cupboard.example;

import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import nl.qbusict.cupboard.example.model.Book;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;
import static nl.qbusict.cupboard.example.content.BooksContentProvider.AUTHOR_URI;
import static nl.qbusict.cupboard.example.content.BooksContentProvider.BOOKS_URI;

public class BookActivity extends ListActivity implements LoaderCallbacks<Cursor> {

    private static class BooksAdapter extends CursorAdapter {

        private static class ViewHolder {
            private final TextView text1;
            private final TextView text2;

            public ViewHolder(View view) {
                text1 = (TextView) view.findViewById(android.R.id.text1);
                text2 = (TextView) view.findViewById(android.R.id.text2);
            }
        }

        public BooksAdapter(Context context) {
            super(context, null);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Book book = cupboard().withCursor(cursor).get(Book.class);
            // fetch the author entity too
            book.author = cupboard().withContext(context).get(AUTHOR_URI, book.author);
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.text1.setText(book.title);
            holder.text2.setText(book.author.name);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
            view.setTag(new ViewHolder(view));
            return view;
        }

    }

    private BooksAdapter mAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new BooksAdapter(this);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(this);
        loader.setUri(BOOKS_URI);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
