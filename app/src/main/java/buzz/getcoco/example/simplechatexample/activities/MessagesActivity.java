package buzz.getcoco.example.simplechatexample.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import buzz.getcoco.example.simplechatexample.Utilities.Constants;
import buzz.getcoco.example.simplechatexample.adapter.RecyclerViewAdapter;
import buzz.getcoco.example.simplechatexample.classes.Message;
import buzz.getcoco.example.simplechatexample.databinding.ActivityMessagesBinding;
import buzz.getcoco.media.MediaSession;
import java.util.Locale;

public class MessagesActivity extends AppCompatActivity {
  public static final String TAG = "MessagesActivity";
  private MediaSession session;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String networkId = getIntent().getStringExtra(Constants.NETWORK_ID);
    long destinationNodeId = getIntent().getLongExtra(Constants.NODE_ID, -1);

    session = new MediaSession.JoinBuilder(this)
        .setSessionId(networkId)
        .build();

    session.create().observe(this, response -> {
      Log.d(TAG, "onCreate: create session response: " + response);

      if (null != response.getError()) {
        Toast
            .makeText(MessagesActivity.this, "error: "
                + response.getError().getMessage(), Toast.LENGTH_SHORT)
            .show();
        return;
      }

      Log.d(TAG, "onCreate: session joined in messages activity");
      Toast.makeText(this, "Session Joined", Toast.LENGTH_SHORT).show();
    });

    long[] destinationNodeIds = (-1 == destinationNodeId) ? null : new long[]{destinationNodeId};

    ActivityMessagesBinding binding = ActivityMessagesBinding.inflate(getLayoutInflater());

    RecyclerViewAdapter adapter = new RecyclerViewAdapter();
    binding.rvMessages.setAdapter(adapter);

    session.setMessageReceivedListener((message, sourceNodeId) -> runOnUiThread(() -> {
      Log.d(TAG, "onCreate: received message" + message);
      adapter.addMessage(new Message(String.format(Locale.US, "%d: %s", sourceNodeId, message),
          RecyclerViewAdapter.RECEIVED_TYPE));
      binding.rvMessages.scrollToPosition(adapter.getItemCount() - 1);
    }));

    // sending using session
    binding.btnSend.setOnClickListener(v -> {
      String messageBody = binding.etEnterMessage.getEditableText().toString();

      session.sendMessage(messageBody, destinationNodeIds);

      adapter.addMessage(new Message(messageBody, RecyclerViewAdapter.SENT_TYPE));
      binding.rvMessages.scrollToPosition(adapter.getItemCount() - 1);
      binding.etEnterMessage.setText("");
      Log.d(TAG, "onCreate: message sent" + messageBody);
    });

    session.getConnectionStatus().observe(this, state -> {
      Log.d(TAG, "onCreate: status: " + state);
      Toast.makeText(this, "status: " + state, Toast.LENGTH_SHORT).show();
    });

    session.setNodeStatusListener((sourceNodeId, isOnline) -> runOnUiThread(() -> Toast
        .makeText(this, "Node: " + sourceNodeId + ", online: " + isOnline, Toast.LENGTH_SHORT)
        .show()));

    setContentView(binding.getRoot());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (null != session) {
      session.destroy();
    }
  }
}
