package bomberman.model;

public class Economer {
    Topic topic;
    int ctr;

    public Economer(){
        topic = Topic.DOWN;
        ctr = 0;
    }

    public int getCtr() {
        return ctr;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setCtr(int ctr) {
        this.ctr = ctr;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }
}
