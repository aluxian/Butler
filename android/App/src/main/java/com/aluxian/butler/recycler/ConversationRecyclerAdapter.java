package com.aluxian.butler.recycler;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aluxian.butler.R;
import com.aluxian.butler.recycler.items.ContactPickerItem;
import com.aluxian.butler.recycler.items.FreebaseItem;
import com.aluxian.butler.recycler.items.TextItem;
import com.aluxian.butler.utils.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Adapter that manages the items of the conversation recycler view
 */
public class ConversationRecyclerAdapter extends RecyclerView.Adapter<ConversationItem.ViewHolder> {

    /** Logger instance */
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger LOG = new Logger(ConversationRecyclerAdapter.class);

    /** Holds the item types and their layout IDs */
    private static HashMap<Class, Integer> ITEM_TYPES = new HashMap<>();

    /** Add the item types */
    static {
        ITEM_TYPES.put(TextItem.class, R.layout.item_text);
        ITEM_TYPES.put(FreebaseItem.class, R.layout.item_freebase);
        ITEM_TYPES.put(ContactPickerItem.class, R.layout.item_contact_picker);
    }

    /** Items list */
    private ArrayList<ConversationItem> items = new ArrayList<>();

    /** The LayoutManager of the recycler list view */
    private LinearLayoutManager linearLayoutManager;

    /**
     * Create a new adapter object
     *
     * @param linearLayoutManager The LayoutManager of the RecyclerView
     */
    public ConversationRecyclerAdapter(LinearLayoutManager linearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager;
    }

    @Override
    public ConversationItem.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        try {
            int layoutId = (Integer) ITEM_TYPES.values().toArray()[viewType];

            // Use reflection to create a ViewHolder
            Class itemClass = (Class) ITEM_TYPES.keySet().toArray()[viewType];
            Class<?> viewHolderClass = Class.forName(itemClass.getName() + "$ViewHolder");
            Constructor viewHolderConstructor = viewHolderClass.getConstructor(View.class);
            Object viewHolder = viewHolderConstructor.newInstance(inflater.inflate(layoutId, parent, false));

            return (ConversationItem.ViewHolder) viewHolder;
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            LOG.e(e);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(ConversationItem.ViewHolder viewHolder, int position) {
        items.get(position).bindViewHolder(viewHolder);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return new ArrayList<>(ITEM_TYPES.keySet()).indexOf(items.get(position).getClass());
    }

    public ArrayList<ConversationItem> getItemsList() {
        return items;
    }

    /**
     * Adds a new item into the list
     *
     * @param item Item to be added
     */
    public void addItem(ConversationItem item) {
        int index = items.size();
        items.add(item);
        notifyItemInserted(index);
        linearLayoutManager.scrollToPosition(index);
    }

    /**
     * Removes an item from the list
     *
     * @param item Item to remove
     */
    public void removeItem(ConversationItem item) {
        int index = items.indexOf(item);
        items.remove(item);
        notifyItemInserted(index);
    }

    /**
     * Notify the adapter that an item has changed
     *
     * @param item The item that has changed
     */
    public void changedItem(ConversationItem item) {
        notifyItemChanged(items.indexOf(item));
    }

}
