package lab.s2jh.module.sys.service;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import lab.s2jh.core.cons.GlobalConstant;
import lab.s2jh.core.dao.jpa.BaseDao;
import lab.s2jh.core.exception.ServiceException;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.util.DateUtils;
import lab.s2jh.module.sys.dao.SmsVerifyCodeDao;
import lab.s2jh.module.sys.entity.SmsVerifyCode;
import lab.s2jh.support.service.DynamicConfigService;

import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SmsVerifyCodeService extends BaseService<SmsVerifyCode, Long> {

    private static final Logger logger = LoggerFactory.getLogger(SmsVerifyCodeService.class);

    private final static int VERIFY_CODE_LIVE_MINUTES = 60;

    @Autowired
    private SmsVerifyCodeDao smsVerifyCodeDao;

    @Autowired
    private DynamicConfigService dynamicConfigService;

    @Override
    protected BaseDao<SmsVerifyCode, Long> getEntityDao() {
        return smsVerifyCodeDao;
    }

    /**
     * Phone-based random number generator 6 codes
     * @param MobileNum phone number
     * @return 6 -bit random number
     */
    public String generateSmsCode(HttpServletRequest request, String mobileNum, boolean mustExist) {
        if (mustExist && dynamicConfigService.getBoolean(GlobalConstant.cfg_public_send_sms_disabled, false)) {
            throw new ServiceException("Non-registered phone number sending text messages has been suspended");
        }

        SmsVerifyCode smsVerifyCode = smsVerifyCodeDao.findByMobileNum(mobileNum);
        boolean updateCode = false;
        if (smsVerifyCode == null) {

        	// Must have successfully verified before the phone number requested
            if (mustExist && !DynamicConfigService.isDevMode()) {
                throw new ServiceException("手机号无效");
            }

            updateCode = true;
            smsVerifyCode = new SmsVerifyCode();
            smsVerifyCode.setMobileNum(mobileNum);
        } else {

        	//expired
            if (DateUtils.currentDate().after(smsVerifyCode.getExpireTime())) {
                updateCode = true;
            }
        }


     // If you need to update the verification code refresh , otherwise not expired before direct return codes
        if (updateCode) {
            String code = RandomStringUtils.randomNumeric(6);
            smsVerifyCode.setCode(code);
            smsVerifyCode.setGenerateTime(DateUtils.currentDate());

         // 5 minutes Validity
            smsVerifyCode.setExpireTime(new DateTime(smsVerifyCode.getGenerateTime()).plusMinutes(VERIFY_CODE_LIVE_MINUTES).toDate());
            smsVerifyCodeDao.save(smsVerifyCode);
        }

        return smsVerifyCode.getCode();
    }

    /**
     * Phone verification codes effectiveness
     * @param MobileNum phone number
     * @param Code verification code
     * @return Boolean whether effective
     */
    public boolean verifySmsCode(HttpServletRequest request, String mobileNum, String code) {

    	// If the development mode, as the default codes 123456 through always , facilitate the development of Test
        if (DynamicConfigService.isDevMode()) {
            if ("123456".equals(code)) {
                return true;
            }
        }
        SmsVerifyCode smsVerifyCode = smsVerifyCodeDao.findByMobileNum(mobileNum);

     // Record the codes found
        if (smsVerifyCode == null) {
            return false;
        }
        Date now = DateUtils.currentDate();

     // Code expired
        if (smsVerifyCode.getExpireTime().before(now)) {
            return false;
        }
        boolean pass = smsVerifyCode.getCode().equals(code);
        if (pass) {
            if (smsVerifyCode.getFirstVerifiedTime() == null) {
                smsVerifyCode.setFirstVerifiedTime(now);
            }
            smsVerifyCode.setTotalVerifiedCount(smsVerifyCode.getTotalVerifiedCount() + 1);
            smsVerifyCode.setLastVerifiedTime(now);
            smsVerifyCodeDao.save(smsVerifyCode);
        }
        return pass;
    }

    /**
     * The timing of the verification code timeout removed
     */
    //@Scheduled(fixedRate = 60 * 60 * 1000)
    public void removeExpiredDataTimely() {
        logger.debug("Timely trigger removed expired verify code cache data at Thread: {}...", Thread.currentThread().getId());
        if (smsVerifyCodeDao.countTodoItems() > 0) {
            int effectiveCount = smsVerifyCodeDao.batchDeleteExpireItems(DateUtils.currentDate());
            logger.debug("Removed expired verify code cache data number: {}", effectiveCount);
        }
    }
}
