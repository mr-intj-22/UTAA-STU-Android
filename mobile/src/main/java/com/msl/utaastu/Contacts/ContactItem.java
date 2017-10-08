package com.msl.utaastu.Contacts;

import android.support.annotation.Keep;

/**
 * Created by Malek Shefat on 7/17/2017.
 */

@Keep
class ContactItem {

    private String title, label, content;
    private int type;

    public String getTitle() {
        return title;
    }

    public ContactItem setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public ContactItem setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getContent() {
        return content;
    }

    public ContactItem setContent(String content) {
        this.content = content;
        return this;
    }

    public int getType() {
        return type;
    }

    public ContactItem setType(int type) {
        this.type = type;
        return this;
    }
}
