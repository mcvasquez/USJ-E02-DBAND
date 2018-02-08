package com.e2.practicafinal.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mcvasquez on 1/28/18.
 */

@IgnoreExtraProperties
public class Person {

    public String name;
    public String phone;
    public Map<String, Pet> pets = new HashMap<>();

    public Person() {
    }

    public Person(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("phone", phone);
        result.put("pets", pets);

        return result;
    }
}