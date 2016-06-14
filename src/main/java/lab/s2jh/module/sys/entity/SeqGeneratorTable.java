package lab.s2jh.module.sys.entity;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * The data table structure definitions required for using @see TableSeqGenerator
 */
@Getter
@Setter
@Access(AccessType.FIELD)
@Entity
@Table(name = "seq_generator_table")
public class SeqGeneratorTable {

    @Id
    private String id;

    @Column(name = "initial_value")
    private Integer initialValue;

    @Column(name = "increment_size")
    private Integer incrementSize;

    @Column(name = "next_val")
    private Long nextVal;

}
