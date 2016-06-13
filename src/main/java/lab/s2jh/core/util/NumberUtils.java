package lab.s2jh.core.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class NumberUtils {

    private static final int DEF_DIV_SCALE = 10;

    private NumberUtils() {
    }

    public static BigDecimal round(BigDecimal decimal) {
        return new BigDecimal(Math.round(decimal.doubleValue()));
    }

    public static BigDecimal nullToZero(BigDecimal num) {
        if (num == null) {
            return new BigDecimal("0");
        }
        return num;
    }

    public static Long nullToZero(Long num) {
        if (num == null) {
            return new Long("0");
        }
        return num;
    }

    public static BigDecimal nullToZero(String num) {
        if (num == null) {
            return new BigDecimal("0");
        }
        return new BigDecimal(num);
    }

    public static Integer nullToZero(Integer num) {
        if (num == null) {
            return new Integer("0");
        }
        return num;
    }

    public static boolean isNumber(String num) {
        if (num == null) {
            return false;
        }
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(num);
        return m.matches();
    }

    public static boolean isNumberForImport(String num) {
        Pattern p = Pattern.compile("(\\d+)|(\\d+.{1}\\d+)");
        Matcher m = p.matcher(num);
        if (m.matches()) {

            /** Decimal and decimal places is not zero */
            Float fNum = Float.parseFloat(num);
            return fNum.floatValue() == fNum.longValue();
        }
        return false;
    }

    public static boolean isFloat(String num) {
        Pattern p = Pattern.compile("(\\d+)|(\\d+.{1}\\d+)");
        Matcher m = p.matcher(num);
        return m.matches();
    }

    public static boolean isInteger(String num) {
        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(num);
        return m.matches();
    }

    /**
     * A designated number of decimal places
     * @param num
     * @param floatnum
     * @return
     */
    public static boolean isFloat(String num, int floatnum) {
        if (floatnum == 0) {
            return isInteger(num);
        }
        Pattern p = Pattern.compile("(\\d+)|(\\d+.{1}\\d{0," + floatnum + "})");
        Matcher m = p.matcher(num);
        return m.matches();
    }

    /**
     * Provide accurate addition.
     *
     * @param V1
     * Summand
     * @param V2
     * Addend
     * @return Two arguments
     */
    public static BigDecimal add(BigDecimal v1, BigDecimal v2) {
        BigDecimal sum = new BigDecimal(0);

     // Set a value other than 0
        if (v1 == null || v2 == null) {
            if (v1 == null && v2 == null) {
                return sum;
            }
            return v1 == null ? v2 : v1;
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1.doubleValue()));
        BigDecimal b2 = new BigDecimal(Double.toString(v2.doubleValue()));
        return b1.add(b2);
    }

    /**
     * Provide accurate subtraction .
     *
     * @param V1
     * Minuend
     * @param V2
     * Subtrahend
     * Poor @return two parameters
     */
    public static BigDecimal sub(BigDecimal v1, BigDecimal v2) {
        BigDecimal b1 = new BigDecimal("0");
        if (v1 != null) {
            b1 = new BigDecimal(Double.toString(v1.doubleValue()));
        }
        BigDecimal b2 = new BigDecimal("0");
        if (v2 != null) {
            b2 = new BigDecimal(Double.toString(v2.doubleValue()));
        }
        return b1.subtract(b2);
    }

    /**
     * Compare the size of two numbers
     *
     * @param V1
     * The first number
     * @param V2
     * The second number
     * @return Also returned more than 1 , equal to 0 back further , also less than or -1
     */
    public static int compare(BigDecimal v1, BigDecimal v2) {
        int intValue = 0;
        Double d1 = v1.doubleValue();
        Double d2 = v2.doubleValue();
        if (d1 > d2) {
            intValue = 1;
        }
        if (d1 == d2) {
            intValue = 0;
        }
        if (d1 < d2) {
            intValue = -1;
        }
        return intValue;
    }

    /**
     * It provides accurate multiplication .
     *
     * @param V1
     * Multiplicand
     * @param V2
     * Multiplier
     * @return The product of two arguments
     */
    public static BigDecimal mul(BigDecimal v1, BigDecimal v2) {
        if (v1 == null || v2 == null) {
            return new BigDecimal(0);
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1.doubleValue()));
        BigDecimal b2 = new BigDecimal(Double.toString(v2.doubleValue()));

        return b1.multiply(b2);
    }

    /**
     * It provides accurate multiplication .
     *
     * @param V1
     * Multiplicand
     * @param V2
     * Multiplier
     * @return The product of two arguments
     */
    public static BigDecimal mul(Long v1, BigDecimal v2) {
        if (v1 == null || v2 == null) {
            return new BigDecimal(0);
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1.doubleValue()));
        BigDecimal b2 = new BigDecimal(Double.toString(v2.doubleValue()));
        return b1.multiply(b2);
    }

    /**
     * Provide ( relatively ) precise division operation , when a situation will not divide when accurate to 10 decimal point , after rounding numbers .
     *
     * @param V1
     * Dividend
     * @param V2
     * Divisor
     * @return The quotient of the two parameters
     */
    public static BigDecimal div(BigDecimal v1, BigDecimal v2) {
        return div(v1, v2, DEF_DIV_SCALE);
    }

    /**
     * Providing ( relatively ) accurate division. When occurrence will not divide when the scale parameter specified accuracy , after rounding numbers .
     *
     * @param V1
     * Dividend
     * @param V2
     * Divisor
     * @param Scale
     * Indicates expressed the need for accurate to several decimal point .
     * @return The quotient of the two parameters
     */
    public static BigDecimal div(BigDecimal v1, BigDecimal v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1.doubleValue()));
        BigDecimal b2 = new BigDecimal(Double.toString(v2.doubleValue()));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Provide accurate decimal rounding process .
     *
     * @param V
     * Requires rounded figures
     * @param Scale
     * After the decimal point a few
     * @return The result after rounding
     */
    public static BigDecimal round(BigDecimal v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v.doubleValue()));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Numbers, currency format
     * @version 2011-9-21 09:19:39 PM
     * @param pattern
     * @param number
     * @return
     */
    public static String numberFormat(String pattern, BigDecimal number) {
        String numberStr = null;
        if (number == null) {
            return "";
        }
        try {
            if (pattern == null || pattern.equals("")) {
                numberStr = new DecimalFormat("#0.00##").format(number.doubleValue());
            } else {
                numberStr = new DecimalFormat(pattern).format(number.doubleValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return numberStr;
    }

    /**
     * After accurate to two decimal places
     */
    public static String isDouble(String num) {
        if (num.equals("-") || StringUtils.isBlank(num)) {
            return "-";
        } else {
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            return decimalFormat.format(Double.valueOf(num));
        }
    }

    /**
     * Returns negative
     * @param num
     * @return
     */
    public static BigDecimal negative(BigDecimal num) {
        return NumberUtils.mul(num, new BigDecimal(-1));
    }


 // Binary hex
    public static String binaryString2hexString(String bString) {
        if (bString == null || bString.equals("") || bString.length() % 8 != 0)
            return null;
        StringBuffer tmp = new StringBuffer();
        int iTmp = 0;
        for (int i = 0; i < bString.length(); i += 4) {
            iTmp = 0;
            for (int j = 0; j < 4; j++) {
                iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);
            }
            tmp.append(Integer.toHexString(iTmp));
        }
        return tmp.toString();
    }


 // Hex binary transfer
    public static String hexString2binaryString(String hexString) {
        if (hexString == null || hexString.length() % 2 != 0)
            return null;
        String bString = "", tmp;
        for (int i = 0; i < hexString.length(); i++) {
            tmp = "0000" + Integer.toBinaryString(Integer.parseInt(hexString.substring(i, i + 1), 16));
            bString += tmp.substring(tmp.length() - 4);
        }
        return bString;
    }

    public static void main(String[] args) {
        // System.out.println((float)Math.ceil(5.5*10)/10);
        System.out.println(NumberUtils.numberFormat("#,##0.0000", new BigDecimal("53232332.3656")));
        System.out.println(NumberUtils.sub(new BigDecimal(6.0), new BigDecimal(5.3)));
        System.out.println(NumberUtils.compare(NumberUtils.sub(new BigDecimal(4900.23), new BigDecimal(4000)), BigDecimal.ZERO));
       

    }

}
