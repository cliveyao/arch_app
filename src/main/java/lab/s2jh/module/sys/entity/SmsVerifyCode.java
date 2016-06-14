package lab.s2jh.module.sys.entity;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.entity.BaseNativeEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Getter
@Setter
@Accessors(chain = true)
@Access(AccessType.FIELD)
@Entity
@Table(name = "sys_SmsVerifyCode")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@MetaData(value = "SMS verification code")
public class SmsVerifyCode extends BaseNativeEntity {

    private static final long serialVersionUID = 615208416034164816L;

    @Column(length = 32, nullable = false, unique = true)
    private String mobileNum;

    @Column(length = 32, nullable = false)
    private String code;

    @Column(nullable = false)
    private Date generateTime;

    @MetaData(value = "Expiration", comments = "Timing regular cleaning task expired checksum")
    @Column(nullable = false)
    private Date expireTime;

    @MetaData(value = "By the time the first verification", comments = "Phone number verified through preserved")
    @Column(nullable = true)
    private Date firstVerifiedTime;

    @MetaData(value = "Last verified by time", comments = "Phone number verified through preserved")
    @Column(nullable = true)
    private Date lastVerifiedTime;

    @MetaData(value = "Total verified by frequency")
    @Column(nullable = true)
    private Integer totalVerifiedCount = 0;
}
