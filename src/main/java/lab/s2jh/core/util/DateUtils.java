package lab.s2jh.core.util;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lab.s2jh.core.service.Validation;
import lab.s2jh.support.service.DynamicConfigService;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtils {

    private final static Logger logger = LoggerFactory.getLogger(DateUtils.class);

    public final static String DEFAULT_TIMEZONE = "GMT+8";

    public final static String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    public final static String ISO_SHORT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public final static String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public final static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    public final static String SHORT_TIME_FORMAT = "yyyy-MM-dd HH:mm";

    public final static String FULL_SEQ_FORMAT = "yyyyMMddHHmmssSSS";

    public final static String[] MULTI_FORMAT = { DEFAULT_DATE_FORMAT, DEFAULT_TIME_FORMAT, ISO_DATE_TIME_FORMAT, ISO_SHORT_DATE_TIME_FORMAT,
            SHORT_TIME_FORMAT, "yyyy-MM" };

    public final static String FORMAT_YYYY = "yyyy";

    public final static String FORMAT_YYYYMM = "yyyyMM";

    public final static String FORMAT_YYYYMMDD = "yyyyMMdd";

    public final static String FORMAT_YYYYMMDDHH = "yyyyMMddHH";

    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(date);
    }

    public static String formatDate(Date date, String format) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(format).format(date);
    }

    public static Integer formatDateToInt(Date date, String format) {
        if (date == null) {
            return null;
        }
        return Integer.valueOf(new SimpleDateFormat(format).format(date));
    }

    public static Long formatDateToLong(Date date, String format) {
        if (date == null) {
            return null;
        }
        return Long.valueOf(new SimpleDateFormat(format).format(date));
    }

    public static String formatTime(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(DEFAULT_TIME_FORMAT).format(date);
    }

    public static String formatShortTime(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(SHORT_TIME_FORMAT).format(date);
    }

    public static String formatDateNow() {
        return formatDate(DateUtils.currentDate());
    }

    public static String formatTimeNow() {
        return formatTime(DateUtils.currentDate());
    }

    public static Date parseDate(String date, String format) {
        if (date == null) {
            return null;
        }
        try {
            return new SimpleDateFormat(format).parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static Date parseTime(String date, String format) {
        if (date == null) {
            return null;
        }
        try {
            return new SimpleDateFormat(format).parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static Date parseDate(String date) {
        return parseDate(date, DEFAULT_DATE_FORMAT);
    }

    public static Date parseTime(String date) {
        return parseTime(date, DEFAULT_TIME_FORMAT);
    }

    public static String plusOneDay(String date) {
        DateTime dateTime = new DateTime(parseDate(date).getTime());
        return formatDate(dateTime.plusDays(1).toDate());
    }

    public static String plusOneDay(Date date) {
        DateTime dateTime = new DateTime(date.getTime());
        return formatDate(dateTime.plusDays(1).toDate());
    }

    public static String getHumanDisplayForTimediff(Long diffMillis) {
        if (diffMillis == null) {
            return "";
        }
        long day = diffMillis / (24 * 60 * 60 * 1000);
        long hour = (diffMillis / (60 * 60 * 1000) - day * 24);
        long min = ((diffMillis / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long se = (diffMillis / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        StringBuilder sb = new StringBuilder();
        if (day > 0) {
            sb.append(day + "D");
        }
        DecimalFormat df = new DecimalFormat("00");
        sb.append(df.format(hour) + ":");
        sb.append(df.format(min) + ":");
        sb.append(df.format(se));
        return sb.toString();
    }

    /**
     * Similar 2014-01-01 ~ 2014-01-30 the format into a single string to a two -element array
     */
    public static Date[] parseBetweenDates(String date) {
        if (StringUtils.isBlank(date)) {
            return null;
        }
        date = date.replace("～", "~");
        Date[] dates = new Date[2];
        String[] values = date.split("~");
        dates[0] = parseMultiFormatDate(values[0].trim());
        dates[1] = parseMultiFormatDate(values[1].trim());
        return dates;
    }

    public static Date parseMultiFormatDate(String date) {
        try {
            return org.apache.commons.lang3.time.DateUtils.parseDate(date, MULTI_FORMAT);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @ Title: getDiffDay
     * @ Description: Get the number of days difference between the date
     * @ Param: @param beginDate string type Start Date
     * @ Param: @param endDate string type end date
     * @ Param: @return
     * @ Return: Long date difference between the number of days
     * @ Author: Xie
     *@thorws:
     */
    public static Long getDiffDay(String beginDate, String endDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Long checkday = 0l;

     // Number of days difference between the beginning of the end
        try {
            checkday = (formatter.parse(endDate).getTime() - formatter.parse(beginDate).getTime()) / (1000 * 24 * 60 * 60);
        } catch (ParseException e) {

            e.printStackTrace();
            checkday = null;
        }
        return checkday;
    }

    /**
    *@Title:getDiffDay
    *@Description: The number of days difference between the acquisition date
    * @ Param: @param beginDate Date Type Start Date
    * @ Param: @param endDate Date End Date Type
    * @ Param: @return
    * @ Return: Long difference between the number of days
    * @ Author: Xie
    *@thorws:
    */
    public static Long getDiffDay(Date beginDate, Date endDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strBeginDate = format.format(beginDate);

        String strEndDate = format.format(endDate);
        return getDiffDay(strBeginDate, strEndDate);
    }

    /**
     * N days
     * @param n
     * @param date
     * @return
     */
    public static Date nDaysAfter(Integer n, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + n);
        return cal.getTime();
    }

    /**
     * N days ago
     * @param n
     * @param date
     * @return
     */
    public static Date nDaysAgo(Integer n, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - n);
        return cal.getTime();
    }

    private static Date currentDate;

    public static Date setCurrentDate(Date date) {
        Validation.isTrue(DynamicConfigService.isDevMode(), "The current operation can only be developed in the test run mode is available");
        if (date == null) {
            currentDate = null;
        } else {
            currentDate = date;
        }
        return currentDate;
    }

    /**
     * In order to facilitate the control of business data in the data simulation program to get the current time
     * Provides a class to help deal with the current time , in order to avoid misuse ,
     *  only devMode development model allowed to " distort " the current time
     * @return
     */
    public static Date currentDate() {
        if (currentDate == null) {
            return new Date();
        }
        if (DynamicConfigService.isDevMode()) {
            return currentDate;
        } else {
            return new Date();
        }
    }

    public static Integer getWeekOfYear(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        String yearString = formatDate(date, FORMAT_YYYY);
        int weekNum = c.get(Calendar.WEEK_OF_YEAR);
        if (weekNum < 10) {
            yearString = StringUtils.rightPad(yearString, 5, "0");
        }
        return Integer.valueOf(yearString + weekNum);
    }

    public static void main(String[] args) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH, 1);
        System.out.println(DateUtils.formatDate(c.getTime(), DateUtils.FORMAT_YYYYMMDD));

        DateUtils.formatDate(new Date(), DEFAULT_DATE_FORMAT);

        Pattern p = Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})");
        Matcher m = p.matcher(DateUtils.formatDate(new Date(), DEFAULT_DATE_FORMAT));

        if (m.find()) {
            System.out.println("date:" + m.group());
            System.out.println("year:" + m.group(1));
            System.out.println("month:" + m.group(2));
            System.out.println("day:" + m.group(3));
        }

        String time = "2015/11/02 ";

        System.out.println(parseDate(time, "yyyy/MM/dd"));
    }

}
