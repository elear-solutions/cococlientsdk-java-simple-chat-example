package buzz.getcoco.example.simplechatexample.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import buzz.getcoco.example.simplechatexample.R;
import buzz.getcoco.example.simplechatexample.Utilities.Constants;
import buzz.getcoco.example.simplechatexample.Utilities.LoginHelper;
import buzz.getcoco.example.simplechatexample.databinding.ActivityMainBinding;
import buzz.getcoco.media.MediaSession;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
  public static final String TAG = "MainActivity";
  MediaSession session;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    LoginHelper.init(this);

    session = new MediaSession.DoNothingBuilder(this).build();

    super.onCreate(savedInstanceState);

    ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());

    binding.btnLogin.setOnClickListener(v -> {
      if (LoginHelper.isLoggedIn()) {
        Log.d(TAG, "onCreate: isLoggedIn");
        login(binding);
        return;
      }

      Editable etBaseUrl = binding.etBaseUrl.getText();
      Editable etUsername = binding.etUserName.getText();

      String baseUrl = (null == etBaseUrl) ? "" : etBaseUrl.toString();
      String username = (null == etUsername) ? "" : etUsername.toString();

      if (baseUrl.isEmpty()) {
        Toast.makeText(this, "enter valid base url", Toast.LENGTH_SHORT).show();
        return;
      }

      if (username.isEmpty()) {
        Toast.makeText(this, "enter valid user name", Toast.LENGTH_SHORT).show();
        return;
      }

      Toast.makeText(this, "logging in", Toast.LENGTH_SHORT).show();

      LoginHelper.setBaseUrl(this, baseUrl);
      LoginHelper.setUsername(this, username);

      login(binding);
    });

    binding.btnJoin.setOnClickListener(v -> {
      String networkId = binding.etNetworkId.getText().toString();

      Log.d(TAG, "onCreate: input networkId: " + networkId);

      if (TextUtils.isEmpty(networkId)) {
        Toast.makeText(this, "Please Fill NetworkId", Toast.LENGTH_LONG).show();
        return;
      }

      session.getAllSessions()
          .observe(this, sessionHandleResponse -> {
            if (null != sessionHandleResponse.getError()) {
              Toast.makeText(this, "error while fetching all sessions", Toast.LENGTH_SHORT).show();
              return;
            }

            List<MediaSession.SessionHandle> handles = (null == sessionHandleResponse.getValue())
                ? Collections.emptyList() : sessionHandleResponse.getValue();

            MediaSession.SessionHandle handle = handles.stream()
                .filter(sessionHandle -> sessionHandle.getId().equals(networkId))
                .findFirst()
                .orElse(null);

            if (null == handle) {
              binding.etNetworkId.setText("");
              Toast
                  .makeText(this, "network ID was not joined, please re-enter", Toast.LENGTH_SHORT)
                  .show();
              return;
            }

            Log.d(TAG, "onCreate: network ID Found");
            Toast.makeText(MainActivity.this, "metadata: "
                + handle.getMetadata()
                + " : "
                + handle.getName(), Toast.LENGTH_SHORT).show();
          });
    });

    binding.btnStartMessaging.setOnClickListener(v -> {
      String nodeIdStr = binding.etDestinationNodeId.getEditableText().toString();
      String networkId = binding.etNetworkId.getText().toString();

      Intent intent = new Intent(MainActivity.this, MessagesActivity.class);

      if (!TextUtils.isEmpty(nodeIdStr)) {
        intent.putExtra(Constants.NODE_ID, Long.parseLong(nodeIdStr));
      }

      if (TextUtils.isEmpty(networkId)) {
        Toast
            .makeText(this, R.string.network_id_field_cannot_be_empty, Toast.LENGTH_SHORT)
            .show();
        return;
      }

      intent.putExtra(Constants.NETWORK_ID, networkId);
      startActivity(intent);
    });

    setContentView(binding.getRoot());
  }

  private void login(ActivityMainBinding binding) {
    LoginHelper.login().observe(this, tokens -> {
      if (null == tokens) {
        Toast.makeText(this, "error fetching tokens, restart app", Toast.LENGTH_SHORT).show();
        return;
      }

      session.setAuthTokens(tokens);
      Toast.makeText(this, "Successfully Logged in", Toast.LENGTH_SHORT).show();

      binding.etBaseUrl.setVisibility(View.GONE);
      binding.etUserName.setVisibility(View.GONE);
      binding.btnLogin.setVisibility(View.GONE);
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (null != session) {
      session.destroy();
    }
  }
}
