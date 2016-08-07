package com.github.jrejaud.models;

import com.google.gson.annotations.Expose;

import io.realm.RealmObject;

/**
 * Created using http://www.jsonschema2pojo.org/
 */
public class Device extends RealmObject {

    public static final String SWITCH = "switch";
    public static final String LOCK = "lock";

    public static final String ON = "on";
    public static final String OFF = "off";
    public static final String TOGGLE = "toggle";


    @Expose
    private String id;
    @Expose
    private String label;
    @Expose
    private String type;
    @Expose
    private String value;

    /**
     *
     * @return
     * The id
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The label
     */
    public String getLabel() {
        return label;
    }

    /**
     *
     * @param label
     * The label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     *
     * @return
     * The type
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     * The type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return
     * The value
     */
    public String getValue() {
        return value;
    }

    /**
     *
     * @param value
     * The value
     */
    public void setValue(String value) {
        this.value = value;
    }


}
