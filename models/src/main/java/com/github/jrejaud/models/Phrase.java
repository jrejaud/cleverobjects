package com.github.jrejaud.models;

import io.realm.RealmObject;

/**
 * Created by jrejaud on 8/6/16.
 */
public class Phrase extends RealmObject {
    private String name;

    public Phrase() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
