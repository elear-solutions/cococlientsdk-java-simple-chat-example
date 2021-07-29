package buzz.getcoco.example.simplechatexample.classes;

public class Message {
  private String messageBody;
  private int type;

  public Message(String messageBody, int type) {
    this.messageBody = messageBody;
    this.type = type;
  }

  public String getMessageBody() {
    return messageBody;
  }

  public void setMessageBody(String messageBody) {
    this.messageBody = messageBody;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }
}
