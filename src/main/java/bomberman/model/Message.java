package bomberman.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class Message {

    private final Topic topic;
    private final String data;
    private int playerId;

    public Message(Topic topic, String data) {
        this.topic = topic;
        this.data = data;
    }

    @JsonCreator
    public Message(@JsonProperty("topic") Topic topic, @JsonProperty("data") JsonNode data) {
        this.topic = topic;
        this.data = data.toString();
    }

    public Topic getTopic() {
        return topic;
    }

    public String getData() {
        return data;
    }

    public Message setPlayerId(int playerId) {
        this.playerId = playerId;
        return this;
    }

    public int getPlayerId() {
        return playerId;
    }

    @Override
    public String toString() {
        return " id: " + playerId + " topic: " + topic + " data: " + data;
    }


}
