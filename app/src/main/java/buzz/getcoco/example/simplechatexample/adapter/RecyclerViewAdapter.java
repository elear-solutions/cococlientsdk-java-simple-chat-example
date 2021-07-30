package buzz.getcoco.example.simplechatexample.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import buzz.getcoco.example.simplechatexample.classes.Message;
import buzz.getcoco.example.simplechatexample.databinding.RecyclerItemReceivedBinding;
import buzz.getcoco.example.simplechatexample.databinding.RecyclerItemSentBinding;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private final List<Message> messages = new ArrayList<>();

  public static final int RECEIVED_TYPE = 1;
  public static final int SENT_TYPE = 2;

  public RecyclerViewAdapter() {
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

    if (viewType == SENT_TYPE) {
      RecyclerItemSentBinding binding = RecyclerItemSentBinding.inflate(layoutInflater);
      return new SentMessageViewHolder(binding);
    }

    RecyclerItemReceivedBinding binding = RecyclerItemReceivedBinding.inflate(layoutInflater);
    return new ReceivedMessageViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    if (SENT_TYPE == messages.get(position).getType()) {
      ((SentMessageViewHolder) holder).binding.tvMessageSent.setText(messages.get(position).getMessageBody());
    } else {
      ((ReceivedMessageViewHolder)holder).binding.tvMessageReceived.setText(messages.get(position).getMessageBody());
    }
  }

  @Override
  public int getItemCount() {
    return messages.size();
  }

  @Override
  public int getItemViewType(int position) {
    return messages.get(position).getType();
  }

  public void addMessage(Message message) {
    messages.add(message);
    notifyItemInserted(messages.size() - 1);
  }

  static final class SentMessageViewHolder extends RecyclerView.ViewHolder {
    final RecyclerItemSentBinding binding;

    public SentMessageViewHolder(@NonNull RecyclerItemSentBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }

  static final class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
    final RecyclerItemReceivedBinding binding;

    public ReceivedMessageViewHolder(@NonNull RecyclerItemReceivedBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
