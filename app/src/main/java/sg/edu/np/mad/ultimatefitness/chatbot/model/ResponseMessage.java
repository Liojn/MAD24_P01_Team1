package sg.edu.np.mad.ultimatefitness.chatbot.model;

public class ResponseMessage {

    String text;
    boolean isMe;

    public ResponseMessage(String text, boolean isMe) {
        this.text = text;
        this.isMe = isMe;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isMe() {
        return isMe;
    }


}