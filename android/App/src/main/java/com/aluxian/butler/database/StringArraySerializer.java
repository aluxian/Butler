package com.aluxian.butler.database;

import android.text.TextUtils;

import com.activeandroid.serializer.TypeSerializer;

/**
 * Serializes a String array into a String and vice-versa
 */
@SuppressWarnings("UnusedDeclaration")
public class StringArraySerializer extends TypeSerializer {

    @Override
    public Class<?> getSerializedType() {
        return String.class;
    }

    @Override
    public Class<?> getDeserializedType() {
        return String[].class;
    }

    @Override
    public String serialize(Object data) {
        if (data == null) {
            return null;
        }

        return "[" + TextUtils.join("][", (String[]) data) + "]";
    }

    @Override
    public String[] deserialize(Object data) {
        if (data == null) {
            return null;
        }

        String str = (String) data;
        str = str.substring(1, str.length() - 1);

        return str.split("\\]\\[");
    }

}
