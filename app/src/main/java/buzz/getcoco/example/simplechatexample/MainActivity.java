package buzz.getcoco.example.simplechatexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import buzz.getcoco.api.CocoClient;
import buzz.getcoco.api.DefaultNativeCallbacksInterface;
import buzz.getcoco.api.Network;
import buzz.getcoco.api.PlatformInterface;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";
  private static final String NETWORK_ID = "5eb99586c46daa001080a24b";

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

    firstStep(getFilesDir(), getString(R.string.client_id));
  }

  private static void setTokens() {

    try {

      CocoClient.getInstance().setTokens("" +
          "{\n" +
          "    \"access_token\": \"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjYwZmNhOTgwZTc4MThiMDAxNjQyZWUwZCJ9.eyJpYXQiOjE2Mjc0MTM5NzYsImV4cCI6MTYyNzQxNzU3Niwic3ViIjoiNWViZDE1YTBlNmY0MmIwMDExN2RmMTBlIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiYXVkIjoiZjA3OTliZGVhM2FlYWRlYjc0MjkiLCJzY29wZSI6WyJhcHAubWdtdCIsImRldmljZS5tZ210IiwibmV0d29yay5tZ210IiwibmV0d29yay5hY2Nlc3MiLCJwcm9maWxlLmFjY2VzcyIsInByb2ZpbGUubWdtdCJdLCJwcm9maWxlIjp7InJvbGVzIjpbIkNvbnN1bWVyIiwiRGV2ZWxvcGVyIiwiT2VtIl19LCJvcGVyYXRvcklkIjoiNWU3MzM4MzhmNmE5NWUwMDE2ZTc5MDJmIiwianRpIjoiOGEwYTBlYjlmMTM0MzFhNCJ9.kjyfJmQpmKzkrgCFXWtssIVlr-nVbniievkusirAjcxi0sK_V1rynugMW6lGm4xLdcZ8QU-X_lfES0Rqho3qOPWQGBwi10PUZRbgU0L3r1fxI-5Df5nUuRwchpyNW8vNRF9zwCIhCahzg1DrdZJJ-gXGkS0gR7mSqRaqLHkc_Yy9Dw924vRaEQrd7dj0UNXtR3ATjhskNlPzEajyoo91qXeD03L986IQnk9lQu7Y4PKVrX0IpY_jIXLs3qOaz7MAsIyuH1ogaDY5dzLvGovQhiMHhTy2Ro9g7EKGksVzry2-4mbUHHG7dvIqlWJ40TpIWFDfn5kcALqAc6CrPS76TQ\",\n" +
          "    \"refresh_token\": \"Dq1oYnEi0T7oT_-YTI0y\",\n" +
          "    \"expires_in\": 3600,\n" +
          "    \"token_type\": \"Bearer\"\n" +
          "}"
      );

    } catch (Exception e) {
      Log.e(TAG, "setTokens: error", e);
    }
  }

  private static void firstStep(File dir, String clientId) {
    Log.d(TAG, "firstStep: started");
    Log.d(TAG, "firstStep: clientId: " + clientId);

    new CocoClient.Builder()
        .addCallbackListener(new DefaultNativeCallbacksInterface() {

          @Override
          public void connectStatusCallback(Network network, Object context) {
            if (!network.getId().equals(NETWORK_ID))
              return;

            Log.d(TAG, "connectStatusCallback: status: " + network.getState());

            if (!network.getState().isConnected())
              return;

            runOnNewThread(MainActivity::fourthStep);
          }

          @Override
          public void networkListCallback(ArrayList<Network> networksList, Object context) {
            runOnNewThread(() -> thirdStep(networksList));
          }

          @Override
          public void receiveDataCallback(Network network, long sourceNodeId, String data) {
            Log.d(TAG, String.format("receiveDataCallback: nodeId: %d, data: %s", sourceNodeId, data));
          }

          @Override
          public void contentInfoCallback(Network network, long sourceNodeId, long contentTime, String data) {
            Log.d(TAG, String.format("contentInfoCallback: contentTime: %d, nodeId: %d, data: %s", contentTime, sourceNodeId, data));
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
            runOnNewThread(MainActivity::setTokens);
          }
        })
        .build();

    setTokens();

    Log.d(TAG, "firstStep: completed");

    runOnNewThread(MainActivity::secondStep);
  }

  private static void secondStep() throws Throwable {
    Log.d(TAG, "secondStep: get all networks");
    CocoClient.getInstance().getAllNetworks(null);
  }

  private static void thirdStep(List<Network> networks) {

    try {
      Thread.sleep(1_000);

      if (null == networks) {
        Log.e(TAG, "networkListCallback: error while get all networks");
        return;
      }

      for (Network n : networks) {

        if (n.getId().equals(NETWORK_ID)) {
          n.connect();
          break;
        }
      }
    } catch (Throwable e) {
      Log.w(TAG, "thirdStep: error", e);
    }
  }

  private static void fourthStep() throws Exception {

    Thread.sleep(5_000);

    Network n = CocoClient.getInstance().getNetwork(NETWORK_ID);

    Log.d(TAG, "fourthStep: using network: " + n);

    Objects.requireNonNull(n);

    n.sendData("hello world", null);
    n.sendContentInfo(System.currentTimeMillis(), "world hello", null);
  }
}
