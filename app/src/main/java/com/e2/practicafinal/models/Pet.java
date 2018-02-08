package com.e2.practicafinal.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mcvasquez on 1/29/18.
 */

@IgnoreExtraProperties
public class Pet {
    public String name;
    public int age = 0;
    public String thumbnail;

    public Pet() {
    }

    public Pet(String name, int age, String thumbnail) {
        this.name = name;
        this.age = age;
        this.thumbnail = thumbnail;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("age", age);
        result.put("thumbnail", thumbnail);

        return result;
    }
}
