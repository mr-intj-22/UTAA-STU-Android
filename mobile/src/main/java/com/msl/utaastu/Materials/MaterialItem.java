package com.msl.utaastu.Materials;

import android.support.annotation.Keep;

/**
 * Created by Malek Shefat on 7/3/2017.
 */

@Keep
public class MaterialItem {

    private String name, desc, link, id;

    public String getLink() {
        return link;
    }

    public MaterialItem setLink(String link) {
        this.link = link;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public MaterialItem setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public String getName() {
        return name;
    }

    public MaterialItem setName(String name) {
        this.name = name;
        return this;
    }

    public String getId() {
        return id;
    }

    public MaterialItem setId(String id) {
        this.id = id;
        return this;
    }
}
