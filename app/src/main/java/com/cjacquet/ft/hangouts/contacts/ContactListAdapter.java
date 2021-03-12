package com.cjacquet.ft.hangouts.contacts;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cjacquet.ft.hangouts.R;
import com.cjacquet.ft.hangouts.activities.EditorActivity;
import com.cjacquet.ft.hangouts.utils.Utils;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.cjacquet.ft.hangouts.database.ContactContract.ContactEntry.CONTENT_URI;

public class ContactListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_INDEX = 0;
    private static final int VIEW_TYPE_CONTACT = 1;
    private static final String CAKE = "\uD83C\uDF82 ";

    private List<ContactSummary> mContactSummaryList;

    public ContactListAdapter(List<ContactSummary> contactSummaryList) {
        mContactSummaryList = contactSummaryList;
        mContactSummaryList.removeAll(Collections.singleton(null));
    }

    @Override
    public int getItemCount() {
        return mContactSummaryList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        ContactSummary contactSummary = mContactSummaryList.get(position);

        if (contactSummary.getIndex() != null) {
            // If the current user is the sender of the message
            return VIEW_TYPE_INDEX;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_CONTACT;
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_INDEX) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_index, parent, false);
            return new ContactListAdapter.IndexViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);
            return new ContactListAdapter.DataViewHolder(view);
        }
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ContactSummary contactSummary = mContactSummaryList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_INDEX:
                ((ContactListAdapter.IndexViewHolder) holder).bind(contactSummary);
                break;
            case VIEW_TYPE_CONTACT:
                ((ContactListAdapter.DataViewHolder) holder).bind(contactSummary);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), EditorActivity.class);

                        Uri currentContactUri = ContentUris.withAppendedId(CONTENT_URI, contactSummary.getId());
                        intent.setData(currentContactUri);

                        v.getContext().startActivity(intent);
                    }
                });
                break;
            default:
                break;
        }
    }

    public void updateData(List<ContactSummary> contacts) {
        mContactSummaryList = contacts;
        mContactSummaryList.removeAll(Collections.singleton(null));
    }

    private static class DataViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFullName;
        private TextView tvPhone;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.name);
            tvFullName.setTextColor(itemView.getContext().getResources().getColor(Utils.getTheme(itemView.getContext()).getPrimaryDarkColorId(), itemView.getContext().getTheme()));
            tvPhone = itemView.findViewById(R.id.summary);
        }

        void bind(ContactSummary contactSummary) {
            String bDay = "";
            String lastname = contactSummary.getLastname();
            String fullname = contactSummary.getName();
            if (contactSummary.getBDay() != null && contactSummary.getBDay().equals(Utils.toStringDate(new Date())))
                bDay += CAKE;
            if (lastname != null && !lastname.isEmpty())
                fullname += " " + lastname;
            tvFullName.setText(bDay + fullname);
            tvPhone.setText(contactSummary.getPhoneNumber());
        }
    }

    private static class IndexViewHolder extends RecyclerView.ViewHolder {
        private TextView tvIndex;

        public IndexViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tvIndex);
            tvIndex.setTextColor(itemView.getContext().getResources().getColor(Utils.getTheme(itemView.getContext()).getPrimaryColorId(), itemView.getContext().getTheme()));
        }

        void bind(ContactSummary contactSummary) {
            tvIndex.setText(contactSummary.getIndex());
        }
    }
}