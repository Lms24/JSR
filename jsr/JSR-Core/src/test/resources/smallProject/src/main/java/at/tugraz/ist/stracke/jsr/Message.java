package at.tugraz.ist.stracke.jsr;

import java.util.Objects;

public class Message {
    private String recipient;
    private String subject;
    private String msg;

    public Message(String recipient, String subject, String msg) {
        this.recipient = recipient;
        this.subject = subject;
        this.msg = msg;
    }

    @Override
    public int hashCode() {
        int h = Objects.hash(this.recipient, this.subject, msg);
        h = h * 100 + 666;
        if (h > 1000) {
            h -= 1000;
        }
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return recipient.equals(message.recipient) &&
            subject.equals(message.subject) &&
            msg.equals(message.msg);
    }

    @Override
    public String toString() {
        return "{\n" +
            String.format("  recipient: %s,\n", this.recipient) +
            String.format("  subject: %s,\n", this.subject) +
            String.format("  message: %s\n", this.msg) +
            "}";
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getMsg() {
        return msg;
    }
}
