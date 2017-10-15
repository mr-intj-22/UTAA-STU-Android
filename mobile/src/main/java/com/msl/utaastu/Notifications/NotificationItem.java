package com.msl.utaastu.Notifications;

import android.support.annotation.Keep;

/**
 * Created by Malek Shefat on 7/14/2017.
 */

@Keep
class NotificationItem {

    private String title, body, topic, date;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
