package com.cjacquet.ft.hangouts.messages;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cjacquet.ft.hangouts.R;
import com.cjacquet.ft.hangouts.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.cjacquet.ft.hangouts.BaseAppCompatActivity.colorTheme;

public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context mContext;
    private List<Message> mMessageList;

    public MessageListAdapter(Context context, List<Message> messageList) {
        mContext = context;
        mMessageList = messageList;
        mMessageList.removeAll(Collections.singleton(null));
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        Message message = mMessageList.get(position);

        if (message == null)
            return -1;
        if (message.getType().equals(MessageType.SENT)) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
            if (viewType == VIEW_TYPE_MESSAGE_SENT) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.my_message_item, parent, false);
                return new SentMessageHolder(view);
            } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.other_message_item, parent, false);
                return new ReceivedMessageHolder(view);
            }
        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
                break;
            default:
                break;
        }
    }

    public void updateData(List<Message> messages, Message newMessage) {
        List<Message> oldMessages;
        if (newMessage == null) {
            oldMessages = messages;
        } else {
            oldMessages = mMessageList;
        }
        mMessageList = new ArrayList<>();
        mMessageList.add(newMessage);
        mMessageList.addAll(oldMessages);
        mMessageList.removeAll(Collections.singleton(null));
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, dateText;
        CardView cardview;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText =  itemView.findViewById(R.id.text_gchat_message_me);
            timeText =  itemView.findViewById(R.id.text_gchat_timestamp_me);
            dateText =  itemView.findViewById(R.id.text_gchat_date_me);
            cardview = itemView.findViewById(R.id.card_gchat_message_me);
        }

        void bind(Message message) {
            messageText.setText(message.getText());
            timeText.setText(Utils.toHoursMinutes(itemView.getContext(), message.getTime()));
            dateText.setText(Utils.toDay(itemView.getContext(), message.getTime()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cardview.setCardBackgroundColor(itemView.getContext().getResources().getColor(colorTheme.getPrimaryColorId(), itemView.getContext().getTheme()));
            }
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, dateText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText =  itemView.findViewById(R.id.text_gchat_message_other);
            timeText =  itemView.findViewById(R.id.text_gchat_timestamp_other);
            dateText =  itemView.findViewById(R.id.text_gchat_date_other);
        }

        void bind(Message message) {
            messageText.setText(message.getText());

            // Format the stored timestamp into a readable String using method.
            timeText.setText(Utils.toHoursMinutes(itemView.getContext(), message.getTime()));
            dateText.setText(Utils.toDay(itemView.getContext(), message.getTime()));
        }
    }
}