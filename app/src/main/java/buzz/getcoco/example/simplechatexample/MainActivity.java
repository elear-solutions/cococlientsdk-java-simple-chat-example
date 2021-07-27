package buzz.getcoco.example.simplechatexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.io.File;

import buzz.getcoco.api.CocoClient;
import buzz.getcoco.api.DefaultNativeCallbacksInterface;
import buzz.getcoco.api.Network;
import buzz.getcoco.api.PlatformInterface;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  static {
    System.loadLibrary("cocojni");
  }

  private static void init(File dir) {
    Log.d(TAG, "init: started");

    new CocoClient.Builder()
        .addCallbackListener(new DefaultNativeCallbacksInterface() {
          @Override
          public void receiveDataCallback(Network network, long sourceNodeId, String data) {
            Log.d(TAG, String.format("receiveDataCallback: nodeId: %d, data: %s", sourceNodeId, data));
          }

          @Override
          public void contentInfoCallback(Network network, long sourceNodeId, long contentTime, String data) {
            Log.d(TAG, String.format("receiveDataCallback: contentTime: %d, nodeId: %d, data: %s", contentTime, sourceNodeId, data));
          }
        })
        .withPlatform(new PlatformInterface() {
          @Override
          public String getCwdPath() {
            Log.d(TAG, "getCwdPath: using path: " + dir.getAbsolutePath());

            return dir.getAbsolutePath();
          }

          @Override
          public String getClientId() {
            return "f0799bdea3aeadeb7429";
          }

          @Override
          public String getAppAccessList() {
            return "{\"appCapabilities\": [ 0 ]}";
          }

          @Override
          public void OAuthCallback(String authorizationEndpoint, String tokenEndpoint) {

            try {
              CocoClient.getInstance().setTokens("" +
                  "{\n" +
                  "    \"access_token\": \"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjYwZmNhOTgwOGNjY2EzMDAxNjExYmY1NyJ9.eyJpYXQiOjE2Mjc0MDU5OTQsImV4cCI6MTYyNzQwOTU5NCwic3ViIjoiNWUwOWNiZWVkMTNlMDkwMDE3YzQ2Y2U3IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiYXVkIjoiZjA3OTliZGVhM2FlYWRlYjc0MjkiLCJzY29wZSI6WyJhcHAubWdtdCIsImRldmljZS5tZ210IiwibmV0d29yay5tZ210IiwibmV0d29yay5hY2Nlc3MiLCJwcm9maWxlLmFjY2VzcyIsInByb2ZpbGUubWdtdCJdLCJwcm9maWxlIjp7InJvbGVzIjpbIkNvbnN1bWVyIiwiRGV2ZWxvcGVyIiwiT2VtIl19LCJvcGVyYXRvcklkIjoiNWU3MzM4MzhmNmE5NWUwMDE2ZTc5MDJmIiwianRpIjoiMTJhNWVjODM3MDI2N2ZiNiJ9.MTBWeI85udWgwkWVACZSMoVJf6XJjhPAN0umUfSis24moHQbEOnWXrTKGPFzyycxnTiztp_rn1SvDX0z3g0zFTYUeMMJoy5vr19QAsRdRG6ylr-vMZ90BYjXmBWvI3DX52UYmsxOu9yVv94IttnKtKSyuVZoNjRARxoCHO3ttf6Lzs_hScvip87xJqpzaleSRU7tZ0ohbHQ_Nte_-DUHhnYKWx3rbnkRN4RXyz1ASZxYpwurRzJgyWxnq5WHK6jcCL_6xJNPaFZuJWps_YHvrrUgZq2ZVF0TEWG-DNj0-OjkQV1rRpx4jk35pa-r6RQCxlQ7RiWo6tU1GcOy7mrJsw\",\n" +
                  "    \"refresh_token\": \"XodLWc3XSJgtepJQg2S-\",\n" +
                  "    \"expires_in\": 3600,\n" +
                  "    \"token_type\": \"Bearer\"\n" +
                  "}"
              );
            } catch (Exception e) {
              Log.e(TAG, "OAuthCallback: error", e);
            }
          }
        })
        .build();

    Log.d(TAG, "init: completed");
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    init(getCacheDir());
  }
}
