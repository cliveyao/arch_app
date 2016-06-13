/**
 * Copyright (c) 2012
 */
package lab.s2jh.core.entity;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lab.s2jh.core.annotation.MetaData;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.AuditOverrides;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Entity object framework provides a basis for the definition of reference of the Native way
 * Specific consider using other keys such as primary increment , sequence , etc. According to the project ,
 *  simply modify the generic parameter types and primary key definitions to comment
 * Each attribute definition can be simply defined MetaData annotations to control the properties of the 
 * meaning of the specific details may view specific code of notes
 */
@Getter
@Setter
@Access(AccessType.FIELD)
@JsonInclude(Include.NON_EMPTY)
@MappedSuperclass
@AuditOverrides({ @AuditOverride(forClass = BaseNativeEntity.class) })
public abstract class BaseNativeEntity extends BaseEntity<Long> {

    private static final long serialVersionUID = 693468696296687126L;

    @MetaData("主键")
    @Id
    @GeneratedValue(generator = "idGenerator")
    @GenericGenerator(name = "idGenerator", strategy = "native")
    @JsonProperty
    private Long id;
}
