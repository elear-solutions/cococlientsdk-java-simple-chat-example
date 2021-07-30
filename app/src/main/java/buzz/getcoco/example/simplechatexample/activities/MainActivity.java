package buzz.getcoco.example.simplechatexample.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;

import buzz.getcoco.api.CocoClient;
import buzz.getcoco.api.DefaultNativeCallbacksInterface;
import buzz.getcoco.api.NativeCallbacksInterface;
import buzz.getcoco.api.Network;
import buzz.getcoco.api.PlatformInterface;
import buzz.getcoco.example.simplechatexample.R;
import buzz.getcoco.example.simplechatexample.Utilities.Constants;
import buzz.getcoco.example.simplechatexample.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
  public static final String TAG = "MainActivity";
  private NativeCallbacksInterface listener;

  static {
    System.loadLibrary("cocojni");
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());

    binding.btnJoin.setOnClickListener(v -> {
      String inviteUrl = binding.etInviteUrl.getText().toString();
      String nodeIdStr = binding.etNodeId.getText().toString();
      String networkId = binding.etNetworkId.getText().toString();

      Log.d(TAG, "onCreate: input invite url: " + inviteUrl);
      Log.d(TAG, "onCreate: input nodeId: " + nodeIdStr);
      Log.d(TAG, "onCreate: input networkId: " + networkId);

      if (TextUtils.isEmpty(networkId)) {
        Toast.makeText(this, "Please Fill NetworkId", Toast.LENGTH_LONG).show();
        return;
      }

      /*
       * If Invite URL is not provided then connecting from saved networks
       * else joining with invite URL
       */
      if (TextUtils.isEmpty(inviteUrl) || TextUtils.isEmpty(nodeIdStr)) {
        Network network = null;

        try {
          Network[] networks = CocoClient.getInstance().getSavedNetworks();
          for (Network n : networks) {
            if (networkId.equals(n.getId())) {
              network = n;
              break;
            }
          }
        } catch (Exception e) {
          Log.d(TAG, "onCreate: err", e);
        }

        if (null != network) {
          try {
            network.connect();
          } catch (Exception e) {
            Log.d(TAG, "onCreate: err", e);
          }
        }
        return;
      }

      long nodeId = Long.parseLong(nodeIdStr);

      runOnNewThread(() -> {
        Log.d(TAG, "onCreate: connecting to network Id: " + networkId);

        new Network.ConnectArgs()
            .setNetworkId(networkId)
            .setNodeId(nodeId)
            .setInviteURL(inviteUrl)
            .setNetworkName("Dummy")
            .setNetworkType(Network.NetworkType.CALL_NET)
            .setUserRole(Network.UserRole.OWNER)
            .setAccessType(Network.AccessType.REMOTE)
            .connect();
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
        Toast.makeText(this, R.string.network_id_field_cannot_be_empty, Toast.LENGTH_SHORT).show();
        return;
      }

      intent.putExtra(Constants.NETWORK_ID, networkId);
      startActivity(intent);
    });

    init(getFilesDir(), getString(R.string.client_id));
    setContentView(binding.getRoot());
    CocoClient.getInstance().addSubscription(listener = new DefaultNativeCallbacksInterface() {
      @Override
      public void connectStatusCallback(Network network, Object context) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "status: " + network.getState() + " : " + network.getName(), Toast.LENGTH_SHORT).show());
      }

      @Override
      public void networkMetadataCallback(Network network) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "metadata: " + network.getMetadata() + " : " + network.getName(), Toast.LENGTH_SHORT).show());
      }
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    CocoClient.getInstance().removeSubscription(listener);
  }

  private static void init(File dir, String clientId) {
    Log.d(TAG, "init: started");
    Log.d(TAG, "init: clientId: " + clientId);

    new CocoClient.Builder()
        .addCallbackListener(new DefaultNativeCallbacksInterface() {
          @Override
          public void receiveDataCallback(Network network, long sourceNodeId, String data) {
            String message = String.format(Locale.US, "receiveDataCallback: nodeId: %d, data: %s", sourceNodeId, data);
            Log.d(TAG, message);
          }

          @Override
          public void contentInfoCallback(Network network, long sourceNodeId, long contentTime, String data) {
            String message = String.format(Locale.US, "contentInfoCallback: contentTime: %d, nodeId: %d, data: %s", contentTime, sourceNodeId, data);
            Log.d(TAG, message);
          }
        })
        .withPlatform(new PlatformInterface() {
          @Override
          public String getCwdPath() {
            Log.d(TAG, "getCwdPath: using path: " + dir.getAbsolutePath());

            return dir.getAbsolutePath();
          }

          @Override
          public String getDownloadPath() {
            return dir.getAbsolutePath();
          }

          @Override
          public String getClientId() {
            return clientId;
          }

          @Override
          public String getAppAccessList() {
            return "{\"appCapabilities\": [ 0 ]}";
          }

          @Override
          public void OAuthCallback(String authorizationEndpoint, String tokenEndpoint) {
          }
        })
        .build();

    Log.d(TAG, "init: completed");
  }

  private static void runOnNewThread(ThrowingRunnable runnable) {
    new Thread(() -> {
      try {
        runnable.run();
      } catch (Throwable tr) {
        Log.e(TAG, "runOnNewThread: error", tr);
      }
    }).start();
  }

  private interface ThrowingRunnable {
    void run() throws Throwable;
  }
}
