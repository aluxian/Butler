package com.aluxian.butler.recycler;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.aluxian.butler.MainActivity;
import com.aluxian.butler.R;
import com.aluxian.butler.database.PatternModel;
import com.aluxian.butler.database.enums.FunctionType;
import com.aluxian.butler.database.models.AssistantCommand;
import com.aluxian.butler.database.pojos.Contact;
import com.aluxian.butler.processing.ProcessorOutput;
import com.aluxian.butler.recycler.items.ContactPickerItem;
import com.aluxian.butler.recycler.items.TextItem;
import com.aluxian.butler.utils.Logger;
import com.aluxian.butler.utils.Utils;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Adapter that manages the items of the conversation recycler view
 */
public class ContactsRecyclerAdapter extends RecyclerView.Adapter<ContactsRecyclerAdapter.ViewHolder> {

    /** Logger instance */
    @SuppressWarnings("UnusedDeclaration") private static final Logger LOG = new Logger(ContactsRecyclerAdapter.class);

    /** Contacts list */
    private List<Contact> mContacts;

    /** What to do onClick */
    private ContactPickerItem.OnClickAction mClickAction;

    /** MainActivity instance */
    private MainActivity mainActivity;

    public ContactsRecyclerAdapter(MainActivity mainActivity, List<Contact> contacts,
                                   ContactPickerItem.OnClickAction clickAction) {
        this.mainActivity = mainActivity;
        this.mContacts = contacts;
        this.mClickAction = clickAction;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder = new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false));

        viewHolder.setContacts(mContacts);
        viewHolder.setClickAction(mClickAction);
        viewHolder.setMainActivity(mainActivity);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Contact contact = mContacts.get(position);
        viewHolder.contactName.setText(contact.displayName);
        viewHolder.contactNumberLabel.setText(contact.label);
        viewHolder.contactNumber.setText(contact.number);
        viewHolder.setPhoneNumber(mContacts.get(position).number);
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    /**
     * ViewHolder class
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        /** The contact's name */
        @InjectView(R.id.contact_name) public TextView contactName;

        /** The label of the given number for the contact */
        @InjectView(R.id.contact_number_label) public TextView contactNumberLabel;

        /** The number of the contact */
        @InjectView(R.id.contact_number) public TextView contactNumber;

        /** The contact's phone number */
        private String phoneNumber;

        /** Contacts list */
        private List<Contact> mContacts;

        /** What to do onClick */
        private ContactPickerItem.OnClickAction mClickAction;

        /** MainActivity instance */
        private MainActivity mainActivity;

        public ViewHolder(final View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            ButterKnife.inject(this, itemView);

            itemView.post(new Runnable() {
                @Override
                public void run() {
                    View recyclerView = (View) itemView.getParent();
                    recyclerView.setMinimumHeight(mContacts.size() * itemView.getHeight());
                }
            });
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public void setContacts(List<Contact> contacts) {
            this.mContacts = contacts;
        }

        public void setClickAction(ContactPickerItem.OnClickAction clickAction) {
            this.mClickAction = clickAction;
        }

        public void setMainActivity(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onClick(View v) {
            switch (mClickAction) {
                case CALL:
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mainActivity.utils.callNumber(phoneNumber);
                        }
                    }, 250);

                    break;

                case TEXT:
                    List<AssistantCommand> parentAssistantCommand = new Select().from(AssistantCommand.class)
                            .where("Function = ?", FunctionType.TEXT).execute();

                    List<? extends PatternModel> expectedInput = new Select().from(AssistantCommand.class)
                            .where("ParentCommand = ?", parentAssistantCommand.get(0).getId()).execute();

                    mainActivity.processorOutputQueue.add(new ProcessorOutput(
                            expectedInput,
                            true,
                            new TextItem("What would you like to tell " +
                                    Utils.capFirstLetter(contactName.getText().toString()) + "?", true)
                    ));

                    break;
            }
        }

    }

}
