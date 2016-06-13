package lab.s2jh.module.bpm;

import java.io.Serializable;

import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *BPM process identifies the entity will conduct the process callbacks , 
 *which writes business object nodes in the current workflow
 * General entities defined in private String activeTaskName property, 
 * and then generate the corresponding setter and getter way to
 */
public interface BpmTrackable {

    @Transient
    @JsonIgnore
    public String getBpmBusinessKey();

    @JsonProperty
    public String getActiveTaskName();

    public BpmTrackable setActiveTaskName(String activeTaskName);

    @JsonProperty
    Serializable getId();
}
