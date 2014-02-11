package info.bigdatahowto.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.UUID;

/**
 * Your basic persistent aggregate root.
 *
 * @author timfulmer
 */
public abstract class AggregateRoot {

    private UUID uuid;
    private Date creationDate;
    private Date modifiedDate;

    protected AggregateRoot() {

        super();

        this.setUuid( UUID.randomUUID());
        this.setCreationDate( new Date());
        this.setModifiedDate( new Date());
    }

    @JsonIgnore
    public abstract String resourceKey();

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public String toString() {
        return "AggregateRoot{" +
                "uuid=" + uuid +
                ", creationDate=" + creationDate +
                ", modifiedDate=" + modifiedDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AggregateRoot that = (AggregateRoot) o;

        return !(uuid != null ? !uuid.equals(that.uuid) : that.uuid != null);

    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }
}
