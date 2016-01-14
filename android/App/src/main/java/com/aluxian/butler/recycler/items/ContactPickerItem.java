package com.aluxian.butler.recycler.items;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.aluxian.butler.MainActivity;
import com.aluxian.butler.R;
import com.aluxian.butler.database.pojos.Contact;
import com.aluxian.butler.main.MainActivityDelegate;
import com.aluxian.butler.recycler.ContactsRecyclerAdapter;
import com.aluxian.butler.recycler.ConversationItem;
import com.aluxian.butler.utils.Logger;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Conversation item that displays text
 */
public class ContactPickerItem extends MainActivityDelegate implements ConversationItem {

    /** Logger instance */
    @SuppressWarnings("UnusedDeclaration") private static final Logger LOG = new Logger(ContactPickerItem.class);

    /** The list of contacts to choose from */
    private List<Contact> mContacts;

    /** What to do when the user clicks on an item */
    private OnClickAction mOnClickAction;

    public ContactPickerItem(MainActivity mainActivity, List<Contact> contacts, OnClickAction onClickAction) {
        super(mainActivity);
        this.mContacts = contacts;
        this.mOnClickAction = onClickAction;
    }

    @Override
    public void bindViewHolder(ConversationItem.ViewHolder baseViewHolder) {
        ViewHolder viewHolder = (ViewHolder) baseViewHolder;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(viewHolder.contactsRecycler.getContext());
        viewHolder.contactsRecycler.setLayoutManager(linearLayoutManager);

        RecyclerView.Adapter contactsRecyclerAdapter = new ContactsRecyclerAdapter(mainActivity, mContacts,
                mOnClickAction);
        viewHolder.contactsRecycler.setAdapter(contactsRecyclerAdapter);
        viewHolder.contactsRecycler.setHasFixedSize(true);
        viewHolder.contactsRecycler.setMinimumHeight(mContacts.size() * 129);
    }

    @Override
    public String getSpeakable() {
        return null;
    }

    @Override
    public boolean isSilent() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * ViewHolder pattern class
     */
    public static class ViewHolder extends ConversationItem.ViewHolder {

        /** The contacts recycler list */
        @InjectView(R.id.contacts_recycler) public RecyclerView contactsRecycler;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

    }

    public static enum OnClickAction {
        CALL, TEXT
    }

}
