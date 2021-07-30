# cococlientsdk-java-simple-chat-example
Sample Android application to create virtual rooms for sharing information and content

# setup for running this app
  ## Get a Client ID using
  - Head to [https://manage.getcoco.buzz](https://manage.getcoco.buzz) (Sign up if needed)
  - Go to Applications tab
  ![step 1](1.png?raw=true "Client ID Step 1")
  - Click on + Application button
  ![step 2](2.png?raw=true "Client ID Step 2")
  - Choose the application name, group and type
  ![step 3](3.png?raw=true "Client ID Step 3")
  - Choose the capabilities and click submit
  ![step 4](4.png?raw=true "Client ID Step 4")
  - Click on the Created Application
  ![step 5](5.png?raw=true "Client ID Step 5")
  - Get the client id present there
  ![step 6](6.png?raw=true "Client ID Step 6")
  ## Running the app
  - Replace the client_id present in [strings.xml](app/src/main/res/values/strings.xml) with your client id
  - Run the app

# setup for other android apps
  - Copy [.jar](app/libs/cococlientsdk-java.jar) and paste it in the app/libs
  - Mark .jar as a gradle dependency
  ```groovy
  implementation fileTree(include: ['*.jar'], dir: 'libs')
  ```
  - Copy [.so](app/src/main/jniLibs) file directories and paste them in app/src/main/jniLibs
  - Add INTERNET permission in Manifest file
  ```xml
  <manifest>
    <uses-permission android:name="android.permission.INTERNET" />
  </manifest>
  ```
  - Load Native Library
  ```java
  class Application {
    static {
      System.loadLibrary("cocojni");
    }
  }
  ```
  - Init CocoClient
  ```java
  class Application {
    public static void main(String[] args) {
      new CocoClient.Builder()
          .addCallbackListener(new DefaultNativeCallbacksInterface() {})
          .withPlatform(new PlatformInterface() {})
          .build();
    }
  }
  ```
  - Connect to a Network with invite url
  ```java
  class Application {
    public static void main(String[] args) {
      // Init CocoClient

      /**
       * POST: https://api.getcoco.buzz/network-manager/networks/:networkId/generate-invite
       * AUTH: Bearer Token {{access_token}}
       * BODY: {
       *        "appId": "{{client_id}}",
       *        "appCapabilities": [ 0, 1, 2 ]
       *       }
       */
      new Network.ConnectArgs()
          .setNetworkId("<NETWORK ID>")
          .setNodeId(NODE_ID)
          .setInviteURL("<INVITE URL>")
          .setNetworkName("<NETWORK NAME>")
          .setNetworkType(NETWORK_TYPE)
          .setUserRole(USER_ROLE)
          .setAccessType(ACCESS_TYPE)
          .connect();
    }
  }
  ```
  - Connect to previously connected networks
  ```java
  class Application {
    public static void main(String[] args) {
      // Init CocoClient

      // Get the list of previously connected networks
      Network[] networks = CocoClient.getInstance().getSavedNetworks();

      for (Network network : networks) {
        network.connect();
      }
    }
  }
  ```
  - Sending Data to other nodes
  ```java
  class Application {
    public static void main(String[] args) {
      // Init CocoClient
      // Connect to a Network

      // Get the list of previously connected networks
      Network[] networks = CocoClient.getInstance().getSavedNetworks();

      // get the network with given network id. (Null if it's not yet connected)
      Network network = CocoClient.getInstance().getNetwork("<NETWORK ID HERE>");

      // send data to all nodes in the network
      network.sendData("hello world", null);

      // send data to nodes with node id 1, 2
      network.sendData("hello world", new long[] { 1, 2 });

      // send content info to all nodes in the network
      network.sendContentInfo(System.currentTimeMillis(), "hello world", null);

      // send data to nodes with node id 1, 2
      network.sendContentInfo(System.currentTimeMillis(), "hello world", new long[] { 1, 2 });
    }
  }
  ```
  - Listening for content info
  ```java
  class Application {
    public static void main(String[] args) {
      // Init CocoClient
      // Connect to a Network

      CocoClient.getInstance().addSubscription(new DefaultNativeCallbacksInterface() {

        @Override
        public void nodeConnectionStatusCallback(Network network, long nodeId, NodeType nodeType, boolean isOnline, Object networkContext) {
          // determines if the node is online or not
          // do something
        }

        @Override
        public void receiveDataCallback(Network network, long sourceNodeId, String data) {
          // do something
        }

        @Override
        public void contentInfoCallback(Network network, long sourceNodeId, long contentTime, String data) {
          // do something
        }
      });
    }
  }
  ```
  - Disconnecting from network
  ```java
  class Application {
    public static void main(String[] args) {
      // Init CocoClient
      // Connect to a Network

      // Get the connected network
      Network network = CocoClient.getInstance().getNetwork("<NETWORK ID>");
      network.disconnect();
    }
  }
  ```
  - Listening for connection status
  ```java
  class Application {
    public static void main(String[] args) {
      // Init CocoClient

      CocoClient.getInstance().addSubscription(new DefaultNativeCallbacksInterface() {
        @Override
        public void connectStatusCallback(Network network, Object context) {
          Network.State status = network.getState();
          // do something
        }
      });
    }
  }
  ```
# NOTE
  - Some of the best practices have been omitted for sake of simplicity and readability
  - Above mentioned APIs are subject to change
  - The manual .jar and .so placement will be replaced with gradle dependency
  - NativeCallbacksInterface will be replaced with inline callback
