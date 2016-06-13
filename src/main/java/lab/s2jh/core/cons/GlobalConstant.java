package lab.s2jh.core.cons;

import java.util.Map;

import lab.s2jh.core.annotation.MetaData;

import com.google.common.collect.Maps;

public class GlobalConstant {

    public static final Map<Boolean, String> booleanLabelMap = Maps.newLinkedHashMap();
    static {
        booleanLabelMap.put(Boolean.TRUE, "Yes");
        booleanLabelMap.put(Boolean.FALSE, "no");
    }

    //gender
    public static enum GenderEnum {
        @MetaData(value = "male")
        M,

        @MetaData(value = "Female")
        F,

        @MetaData(value = "Secrecy")
        U;
    }
    
    @MetaData("ConfigProperty:Title name system")
    public final static String cfg_system_title = "cfg_system_title";

    @MetaData("ConfigProperty:Whether to disable the account registration management function")
    public final static String cfg_mgmt_signup_disabled = "cfg_mgmt_signup_disabled";
   

    @MetaData("ConfigProperty:Whether globally disable sending SMS phone number Open")
    public final static String cfg_public_send_sms_disabled = "cfg_public_send_sms_disabled";

    @MetaData("Data dictionaries: Message Type")
    public final static String DataDict_Message_Type = "DataDict_Message_Type";

}
