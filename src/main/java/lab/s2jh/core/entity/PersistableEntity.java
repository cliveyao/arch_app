package lab.s2jh.core.entity;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Persistable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Getter
@Setter
@Access(AccessType.FIELD)
@MappedSuperclass
@JsonInclude(Include.NON_NULL)
public abstract class PersistableEntity<ID extends Serializable> extends AbstractPersistableEntity<ID> {

    private static final long serialVersionUID = -6214804266216022245L;

    public static final String EXTRA_ATTRIBUTE_GRID_TREE_LEVEL = "level";

    /**
     * When submitting batch processing of data that identifies an object type of operation.@see RevisionType
     */
    public static final String EXTRA_ATTRIBUTE_OPERATION = "operation";

    /**
     * When displaying or submit data that identifies an object as dirty data to be processed
     */
    public static final String EXTRA_ATTRIBUTE_DIRTY_ROW = "dirtyRow";

    /*
     * To quickly determine whether the object is a new state
     * @see org.springframework.data.domain.Persistable#isNew()
     */
    @Transient
    @JsonIgnore
    public boolean isNew() {
        Serializable id = getId();
        return id == null || StringUtils.isBlank(String.valueOf(id));
    }

    /*
     * Used to quickly determine whether the object edit mode
     */
    @Transient
    @JsonIgnore
    public boolean isNotNew() {
        return !isNew();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!getClass().equals(obj.getClass())) {
            return false;
        }
        Persistable that = (Persistable) obj;
        return null == this.getId() ? false : this.getId().equals(that.getId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode += null == getId() ? 0 : getId().hashCode() * 31;
        return hashCode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("Entity of type %s with id: %s", this.getClass().getName(), getId());
    }

    @Transient
    public abstract String getDisplay();

    /**
     * From the extended attribute value to judge whether the current object tag needs to be removed
     * General UI front-end removal operation for the associated collection object elements
     * @return
     */
    @Transient
    @JsonIgnore
    public boolean isMarkedRemove() {
        if (extraAttributes == null) {
            return false;
        }
        Object opParams = extraAttributes.get(EXTRA_ATTRIBUTE_OPERATION);
        if (opParams == null) {
            return false;
        }
        String op = null;
        if (opParams instanceof String[]) {
            op = ((String[]) opParams)[0];
        } else if (opParams instanceof String) {
            op = (String) opParams;
        }
        if ("remove".equalsIgnoreCase(op)) {
            return true;
        }
        return false;
    }
}
