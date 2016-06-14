package lab.s2jh.support.service;

import lab.s2jh.core.annotation.MetaData;

public interface SmsService {
    /**
     *SMS Interface
     * @param SmsContent message content
     * @param MobileNum phone number
     *
     * @return If successful returns null; otherwise, failure to return the exception message
     */
    String sendSMS(String smsContent, String mobileNum, SmsMessageTypeEnum smsType);

    public static enum SmsMessageTypeEnum {
        @MetaData(value = "default", comments = "General program triggered not limited SMS")
        Default,

        @MetaData(value = "registered", comments = "Send SMS for registration , the restrictions can not be less than one minute intervals , no more than 10 times an hour")
        Signup,

        @MetaData(value = "Codes", comments = "For sending SMS codes , restrictions can not be less than one minute intervals , no more than 10 times an hour")
        VerifyCode;

    }
}
