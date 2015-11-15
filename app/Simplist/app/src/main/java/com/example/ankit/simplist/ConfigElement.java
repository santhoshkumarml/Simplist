package com.example.ankit.simplist;

import java.io.Serializable;

/**
 * Created by santhosh on 11/15/15.
 */
public class ConfigElement implements Serializable{
    private String id;
    private String content;

    private static final long serialVersionUID = 42L;

    public ConfigElement(String id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getId() {return  this.id;}
    public String getContent() {return  this.content;}

    @Override
    public String toString() {
        return content;
    }
}
