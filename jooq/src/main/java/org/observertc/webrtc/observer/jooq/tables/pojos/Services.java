/*
 * This file is generated by jOOQ.
 */
package org.observertc.webrtc.observer.jooq.tables.pojos;


import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


/**
 * Services
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Services implements Serializable {

    private static final long serialVersionUID = 207708821;

    private final Integer id;
    private final Integer customerId;
    private final byte[]  uuid;
    private final String  name;
    private final String  description;

    public Services(Services value) {
        this.id = value.id;
        this.customerId = value.customerId;
        this.uuid = value.uuid;
        this.name = value.name;
        this.description = value.description;
    }

    public Services(
        Integer id,
        Integer customerId,
        byte[]  uuid,
        String  name,
        String  description
    ) {
        this.id = id;
        this.customerId = customerId;
        this.uuid = uuid;
        this.name = name;
        this.description = description;
    }

    public Integer getId() {
        return this.id;
    }

    @NotNull
    public Integer getCustomerId() {
        return this.customerId;
    }

    @Size(max = 16)
    public byte[] getUuid() {
        return this.uuid;
    }

    @Size(max = 255)
    public String getName() {
        return this.name;
    }

    @Size(max = 255)
    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Services (");

        sb.append(id);
        sb.append(", ").append(customerId);
        sb.append(", ").append("[binary...]");
        sb.append(", ").append(name);
        sb.append(", ").append(description);

        sb.append(")");
        return sb.toString();
    }
}