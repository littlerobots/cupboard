package nl.qbusict.cupboard.example;

import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import nl.qbusict.cupboard.*;
import nl.qbusict.cupboard.annotation.Column;

/*
 * Sample to show how to work with existing data and @Column annotations
 */
public class ContactsActivity extends ListActivity implements LoaderCallbacks<Cursor> {

    // our entity class, by default entities are _not_ processed, unless enabled for a specific cupboard instance,
    // see below.
    public static class PhoneContact {
        public Long _id;
        @Column("display_name")
        public String name;
        @Column("data1")
        public String phoneNumber;
    }

    private static class ContactsAdapter extends CursorAdapter {

        private static class ViewHolder {
            private final TextView text1;
            private final TextView text2;

            public ViewHolder(View view) {
                text1 = (TextView) view.findViewById(android.R.id.text1);
                text2 = (TextView) view.findViewById(android.R.id.text2);
            }
        }

        private final Cupboard mCupboard;

        public ContactsAdapter(Context context) {
            super(context, null, 0);
            // use a "private" instance of cupboard with annotation support enabled
            mCupboard = new CupboardBuilder().useAnnotations().build();
            // register our entity with this instance
            mCupboard.register(PhoneContact.class);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            PhoneContact contact = mCupboard.withCursor(cursor).get(PhoneContact.class);
            // fetch the author entity too
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.text1.setText(contact.name);
            holder.text2.setText(contact.phoneNumber);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
            view.setTag(new ViewHolder(view));
            return view;
        }

    }

    private ContactsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ContactsAdapter(this);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(this);
        loader.setUri(ContactsContract.Data.CONTENT_URI);
        loader.setSelection(ContactsContract.Data.MIMETYPE + " = ?");
        loader.setSelectionArgs(new String[]{Phone.CONTENT_ITEM_TYPE});
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
