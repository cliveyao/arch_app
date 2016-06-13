package lab.s2jh.core.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *Identity object associated support attachment handling , automatic processing by the 
 *framework bindings and related Annexes cleanup
 * The need to support attachments associated entity object implement this 
 * interface to define a number of attributes associated storage annex
 */
public interface AttachmentableEntity {

    /**
     * The number associated with attachment
     * @return
     */
    @JsonProperty
    public Integer getAttachmentSize();
}
