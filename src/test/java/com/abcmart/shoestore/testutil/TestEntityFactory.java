package com.abcmart.shoestore.testutil;

import java.lang.reflect.Field;

public abstract class TestEntityFactory<T> {

    protected static <T> void setId(T entity, String fieldName, Object idValue) {

        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(entity, idValue);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id field for test", e);
        }
    }
}

