package com.msl.utaastu.Social;

import android.support.annotation.Keep;

/**
 * Created by Malek Shefat on 7/3/2017.
 */

@Keep
public class SocialGroupItem {

    private String name, desc, language, link, id;

    public String getLink() {
        return link;
    }

    public SocialGroupItem setLink(String link) {
        this.link = link;
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public SocialGroupItem setLanguage(String language) {
        this.language = language;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public SocialGroupItem setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public String getName() {
        return name;
    }

    public SocialGroupItem setName(String name) {
        this.name = name;
        return this;
    }

    public String getId() {
        return id;
    }

    public SocialGroupItem setId(String id) {
        this.id = id;
        return this;
    }
}
