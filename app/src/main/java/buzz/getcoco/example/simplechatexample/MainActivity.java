package buzz.getcoco.example.simplechatexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

import buzz.getcoco.api.CocoClient;
import buzz.getcoco.api.DefaultNativeCallbacksInterface;
import buzz.getcoco.api.Network;
import buzz.getcoco.api.PlatformInterface;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  private static final MutableLiveData<String> messageLiveData = new MutableLiveData<>();

  private static String networkId;

  static {
    System.loadLibrary("cocojni");
  }

  private interface ThrowingRunnable {
    void run() throws Throwable;
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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    EditText etNodeId = findViewById(R.id.et_node_id);
    EditText etNetworkId = findViewById(R.id.et_network_id);
    EditText etInviteURL = findViewById(R.id.et_invite_url);
    Button btnJoin = findViewById(R.id.btn_join);

    Objects.requireNonNull(etNodeId);
    Objects.requireNonNull(etNetworkId);
    Objects.requireNonNull(etInviteURL);
    Objects.requireNonNull(btnJoin);

    EditText etDestNodeId = findViewById(R.id.et_destination_node_id);
    Button btnSendData = findViewById(R.id.btn_send_data);
    Button btnSendInfo = findViewById(R.id.btn_send_content_info);

    Objects.requireNonNull(etDestNodeId);
    Objects.requireNonNull(btnSendData);

    EditText etToken = findViewById(R.id.et_token);
    Button btnSet = findViewById(R.id.btn_set);

    Objects.requireNonNull(etToken);
    Objects.requireNonNull(btnSet);

    btnSet.setOnClickListener(v -> {
      String token = etToken.getEditableText().toString();

      Log.d(TAG, "onCreate: using token: " + token);

      runOnNewThread(() -> CocoClient.getInstance().setTokens(token));
    });

    btnJoin.setOnClickListener(v -> {
      String nodeIdStr = etNodeId.getEditableText().toString();
      String networkId = etNetworkId.getEditableText().toString();
      String inviteURL = etInviteURL.getEditableText().toString();

      Log.d(TAG, "onCreate: using nodeId: " + nodeIdStr);
      Log.d(TAG, "onCreate: inviteURL: " + inviteURL);
      Log.d(TAG, "onCreate: networkId: " + networkId);

      if (TextUtils.isEmpty(networkId) || TextUtils.isEmpty(inviteURL) || TextUtils.isEmpty(nodeIdStr)) {
        Network net;

        Toast.makeText(this, "using saved networks to join", Toast.LENGTH_LONG).show();

        try {
          Network[] nets = CocoClient.getInstance().getSavedNetworks();

          if (null == nets || 1 != nets.length) {
            throw new IllegalArgumentException();
          }

          net = nets[0];
          net.connect();

          MainActivity.networkId = net.getId();
        } catch (Exception e) {
          Toast.makeText(this, "error while getting saved networks", Toast.LENGTH_SHORT).show();
        }

        return;
      }

      MainActivity.networkId = networkId;
      long nodeId = Long.parseLong(nodeIdStr);

      runOnNewThread(() -> {

        Log.d(TAG, "onCreate: connecting with invite");

        new Network.ConnectArgs()
            .setNetworkId(networkId)
            .setNodeId(nodeId)
            .setInviteURL(inviteURL)
            .setNetworkName("name")
            .setNetworkType(Network.NetworkType.CALL_NET)
            .setUserRole(Network.UserRole.OWNER)
            .setAccessType(Network.AccessType.REMOTE)
            .connect();

      });
    });

    btnSendData.setOnClickListener(v -> {
      String nodeIdStr = etDestNodeId.getEditableText().toString();

      Log.d(TAG, "onCreate: using nodeId: " + nodeIdStr);

      long[] nodeIds = null;

      try {
        long nodeId = Long.parseLong(nodeIdStr);
        nodeIds = new long[] { nodeId };
      } catch (NumberFormatException nfe) {
        Log.d(TAG, "onCreate: err", nfe);
      }

      long[] finalNodeIds = nodeIds;
      runOnNewThread(() -> {

        Thread.sleep(5_000);

        Network n = CocoClient.getInstance().getNetwork(networkId);

        Log.d(TAG, "thirdStep: using network: " + n);

        Objects.requireNonNull(n);

        n.sendData("hello world", finalNodeIds);
      });
    });

    btnSendInfo.setOnClickListener(v -> {
      String nodeIdStr = etDestNodeId.getEditableText().toString();

      Log.d(TAG, "onCreate: using nodeId: " + nodeIdStr);

      long[] nodeIds = null;

      try {
        long nodeId = Long.parseLong(nodeIdStr);
        nodeIds = new long[] { nodeId };
      } catch (NumberFormatException nfe) {
        Log.d(TAG, "onCreate: err", nfe);
      }

      long[] finalNodeIds = nodeIds;
      runOnNewThread(() -> {

        Thread.sleep(5_000);

        Network n = CocoClient.getInstance().getNetwork(networkId);

        Log.d(TAG, "thirdStep: using network: " + n);

        Objects.requireNonNull(n);

        n.sendContentInfo(System.currentTimeMillis(), "world hello", finalNodeIds);
      });
    });

    messageLiveData.observe(this, message -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());

    init(getFilesDir(), getString(R.string.client_id));
  }

  private static void init(File dir, String clientId) {
    Log.d(TAG, "init: started");
    Log.d(TAG, "init: clientId: " + clientId);

    new CocoClient.Builder()
        .addCallbackListener(new DefaultNativeCallbacksInterface() {

          @Override
          public void connectStatusCallback(Network network, Object context) {
            Log.d(TAG, "connectStatusCallback: status: " + network.getState());

            if (!network.getState().isConnected())
              return;

            messageLiveData.postValue("connected");
          }

          @Override
          public void receiveDataCallback(Network network, long sourceNodeId, String data) {
            String message = String.format(Locale.US, "receiveDataCallback: nodeId: %d, data: %s", sourceNodeId, data);
            Log.d(TAG, message);

            messageLiveData.postValue(message);
          }

          @Override
          public void contentInfoCallback(Network network, long sourceNodeId, long contentTime, String data) {
            String message = String.format(Locale.US, "contentInfoCallback: contentTime: %d, nodeId: %d, data: %s", contentTime, sourceNodeId, data);
            Log.d(TAG, message);

            messageLiveData.postValue(message);
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
          public void OAuthCallback(String authorizationEndpoint, String tokenEndpoint) {}
        })
        .build();

    Log.d(TAG, "init: completed");
  }
}
