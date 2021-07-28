# cococlientsdk-java-simple-chat-example
Sample Android application to create virtual rooms for sharing information and content

# setup for running this app
  ## Get A Client ID using
  - Head to [https://manage.dev.getcoco.buzz](https://manage.dev.getcoco.buzz) (Sign up if needed)
  - Go to Applications tab
  ![step 1](https://github.com/elear-solutions/cococlientsdk-java-simple-chat-example/blob/feature-krishna-add-libs/1.png?raw=true "Client ID Step 1")
  - Click on + Application button
  ![step 2](https://github.com/elear-solutions/cococlientsdk-java-simple-chat-example/blob/feature-krishna-add-libs/2.png?raw=true "Client ID Step 2")
  - Choose the application name, group and type
  ![step 3](https://github.com/elear-solutions/cococlientsdk-java-simple-chat-example/blob/feature-krishna-add-libs/3.png?raw=true "Client ID Step 3")
  - Choose the capabilities and click submit
  ![step 4](https://github.com/elear-solutions/cococlientsdk-java-simple-chat-example/blob/feature-krishna-add-libs/4.png?raw=true "Client ID Step 4")
  - Click on the Created Application
  ![step 5](https://github.com/elear-solutions/cococlientsdk-java-simple-chat-example/blob/feature-krishna-add-libs/5.png?raw=true "Client ID Step 5")
  - Get the client id present there
  ![step 6](https://github.com/elear-solutions/cococlientsdk-java-simple-chat-example/blob/feature-krishna-add-libs/6.png?raw=true "Client ID Step 6")
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
  - Use it
