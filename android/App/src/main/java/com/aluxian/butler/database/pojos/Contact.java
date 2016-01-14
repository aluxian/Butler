package com.aluxian.butler.database.pojos;

import java.io.Serializable;

/**
 * Holds a contact's info
 */
public final class Contact implements Serializable {

    public final String id;

    public final String displayName;

    public final String number;

    public final String label;

    public Contact(String id, String displayName, String number, String label) {
        this.id = id;
        this.displayName = displayName;
        this.number = number;//PhoneNumberUtils.formatNumber(number);
        this.label = label;
    }

}
