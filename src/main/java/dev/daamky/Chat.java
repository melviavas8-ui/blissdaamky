package dev.daamky;

import com.google.gson.JsonObject;

public record Chat(
    long id,
    String channel,
    String senderId,
    String senderName,
    String recipientId,
    String message,
    long timestamp
) {
    public static Chat fromJson(JsonObject json) {
        return new Chat(
            LastSeen.getLong(json, "id"),
            LastSeen.getString(json, "chat"),
            LastSeen.getString(json, "from"),
            LastSeen.getString(json, "fromName"),
            LastSeen.getString(json, "to"),
            LastSeen.getString(json, "text"),
            LastSeen.getLong(json, "ts")
        );
    }

    public boolean isGlobal() {
        return "global".equals(channel);
    }

    public String displayName(String viewerId) {
        if (isGlobal()) {
            return "global";
        }
        return viewerId.equals(senderId) ? recipientId : senderId;
    }

    public long getMessageId() { return id; }
    public String getChannel() { return channel; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getRecipientId() { return recipientId; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}
