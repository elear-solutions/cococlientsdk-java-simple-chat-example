package buzz.getcoco.example.simplechatexample.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import buzz.getcoco.api.CocoClient;
import buzz.getcoco.api.DefaultNativeCallbacksInterface;
import buzz.getcoco.api.Network;
import buzz.getcoco.example.simplechatexample.Utilities.Constants;
import buzz.getcoco.example.simplechatexample.adapter.RecyclerViewAdapter;
import buzz.getcoco.example.simplechatexample.classes.Message;
import buzz.getcoco.example.simplechatexample.databinding.ActivityMessagesBinding;

public class MessagesActivity extends AppCompatActivity {

  private DefaultNativeCallbacksInterface listener;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String networkId = getIntent().getStringExtra(Constants.NETWORK_ID);
    long destinationNodeId = getIntent().getLongExtra(Constants.NODE_ID, -1);

    long[] destinationNodeIds = (-1 == destinationNodeId) ? null : new long[]{destinationNodeId};
    Network network = CocoClient.getInstance().getNetwork(networkId);

    ActivityMessagesBinding binding = ActivityMessagesBinding.inflate(getLayoutInflater());

    RecyclerViewAdapter adapter = new RecyclerViewAdapter();
    binding.rvMessages.setAdapter(adapter);

    CocoClient.getInstance().addSubscription(listener = new DefaultNativeCallbacksInterface() {
      @Override
      public void receiveDataCallback(Network network, long sourceNodeId, String data) {
        runOnUiThread(() -> {
          adapter.addMessage(new Message(String.format(Locale.US, "%d: %s", sourceNodeId, data), RecyclerViewAdapter.RECEIVED_TYPE));
          binding.rvMessages.scrollToPosition(adapter.getItemCount() - 1);
        });
      }
    });

    binding.btnSend.setOnClickListener(v -> {
      String messageBody = binding.etEnterMessage.getEditableText().toString();

      try {
        network.sendData(messageBody, destinationNodeIds);
        adapter.addMessage(new Message(String.format(Locale.US, "Me: %s", messageBody), RecyclerViewAdapter.SENT_TYPE));
      } catch (Exception e) {
        Toast.makeText(this, "Error Sending Message", Toast.LENGTH_SHORT).show();
      }
      binding.rvMessages.scrollToPosition(adapter.getItemCount() - 1);
      binding.etEnterMessage.setText("");
    });

    setContentView(binding.getRoot());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    CocoClient.getInstance().removeSubscription(listener);
  }
}
