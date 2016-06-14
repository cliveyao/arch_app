package s2jh.biz.support.service;

import javax.annotation.PostConstruct;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.support.service.SmsService;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {

    private final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Value("${sms_signature:}")
    private String smsSignature = "";

    @MetaData(value = "Analog transmission mode , this mode directly in the console print SMS message content without actually interface calls")
    @Value("${sms_mock_mode:false}")
    private String smsMockMode;

    private boolean bSmsMockMode;

    @PostConstruct
    public void init() {
        bSmsMockMode = BooleanUtils.toBoolean(smsMockMode);
        if (bSmsMockMode) {
            logger.warn("---SMS Service running at MOCK mode---");
        }
    }

    /**
     * SMS Interface
     * @param SmsContent message content
     * @param MobileNum phone number
     *
     * @return If successful returns null; otherwise, failure to return the exception message
     */
    @Override
    public String sendSMS(String smsContent, String mobileNum, SmsMessageTypeEnum smsType) {
        if (mobileNum == null || mobileNum.length() != 11 || !mobileNum.startsWith("1")) {
            String message = "Invalid mobile number：" + mobileNum;
            logger.warn(message);
            return message;
        }

     // Append signature information
        smsContent += smsSignature;

        if (bSmsMockMode) {
            logger.warn("SMS Service running at MOCK mode, just print SMS content:" + smsContent);
            return null;
        }

        logger.debug("Sending SMS to {} ： {}", mobileNum, smsContent);

     // TODO actual channel interface calls to send SMS
        throw new UnsupportedOperationException("SMS API NOT Implements");
    }
}
