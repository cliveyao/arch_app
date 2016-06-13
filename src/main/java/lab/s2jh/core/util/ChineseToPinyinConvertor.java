package lab.s2jh.core.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.util.CollectionUtils;

/**
 * Utils for convert Chinese to Pinyin
 */
public class ChineseToPinyinConvertor {

    static HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
    static {
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    public static enum MODE {

       // Initials
        capital,
        // Spelling
        whole,
        // First letter and spelling are output
        both
    }

    public static String toPinyinFirst(String chinese, MODE mode) {
        Set<String> pinyins = toPinyin(chinese, mode);
        if (CollectionUtils.isEmpty(pinyins)) {
            return null;
        } else {
            return pinyins.iterator().next();
        }
    }

    public static Set<String> toPinyin(String chinese, MODE mode) {
        List<String> pinyins = new ArrayList<String>();
        List<String> pinyinHeads = new ArrayList<String>();
        char[] arr = chinese.toCharArray();
        try {
            for (char one : arr) {
                int listSize = pinyins.size();
                String[] charPinyins = null;
                //System.out.println("Char: " + one);
                CharType charType = checkType(one);
                if (CharType.NUM.equals(charType) || CharType.LETTER.equals(charType)) {
                    charPinyins = new String[] { String.valueOf(one) };
                } else if (CharType.CHINESE.equals(charType)) {
                    charPinyins = PinyinHelper.toHanyuPinyinStringArray(one, defaultFormat);
                }

                if (charPinyins == null || charPinyins.length == 0) {
                    continue;
                }

               // Initials
                for (int i = 0, len = charPinyins.length; i < len; i++) {
                    charPinyins[i] = StringUtils.capitalize(charPinyins[i]);
                }

                int pinyinSize = charPinyins.length;
                if (listSize == 0) {
                    for (int i = 0; i < pinyinSize; i++) {
                        String py = charPinyins[i];
                        pinyins.add(py);
                        pinyinHeads.add(String.valueOf(py.charAt(0)));
                    }
                } else {
                    if (pinyinSize == 1) {
                        for (int i = 0; i < listSize; i++) {
                            String py = charPinyins[0];
                            pinyins.set(i, pinyins.get(i) + py);
                            pinyinHeads.set(i, pinyinHeads.get(i) + String.valueOf(py.charAt(0)));
                        }
                    } else {
                        List<String> newList = new ArrayList<String>();
                        for (String py : pinyins) {
                            for (String c : charPinyins) {
                                newList.add(py + c);
                            }
                        }
                        pinyins = newList;

                        List<String> newHeadList = new ArrayList<String>();
                        for (String py : pinyinHeads) {
                            for (String c : charPinyins) {
                                newHeadList.add(py + c.charAt(0));
                            }
                        }
                        pinyinHeads = newHeadList;
                    }
                }
            }

        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }

        Set<String> filterDups = new LinkedHashSet<String>();
        if (MODE.capital.equals(mode)) {
            filterDups.addAll(pinyinHeads);
            return filterDups;
        } else if (MODE.whole.equals(mode)) {
            filterDups.addAll(pinyins);
            return filterDups;
        } else {
            filterDups.addAll(pinyins);
            filterDups.addAll(pinyinHeads);
            return filterDups;
        }
    }

    enum CharType {
    	DELIMITER, // As a non-alphabetic characters , for example . ) ( And so on (including U0000-U0080)
        NUM, // 2 -byte number 1234
        LETTER, // gb2312 in , for example : ABC, 2 -byte character contains 1 byte can represent the basic latin and latin-1 OTHER, // other characters
        OTHER, // other characters
        CHINESE; // text
    }

    /**
    * Whether the input variables of type char character type
    * @param C char type variable
    * @return CharType character types
    */
    private static CharType checkType(char c) {
        CharType ct = null;

     // Chinese , coding interval 0x4e00-0x9fbb                
        if ((c >= 0x4e00) && (c <= 0x9fbb)) {
            ct = CharType.CHINESE;
        }
        //Halfwidth and Fullwidth Forms， Encoding range 0xff00-0xffef
        else if ((c >= 0xff00) && (c <= 0xffef)) { //2 -byte words in English
            if (((c >= 0xff21) && (c <= 0xff3a)) || ((c >= 0xff41) && (c <= 0xff5a))) {
                ct = CharType.LETTER;
            }

         // 2-byte number                      
            else if ((c >= 0xff10) && (c <= 0xff19)) {
                ct = CharType.NUM;
            }
         // Other characters , punctuation may be considered
            else
                ct = CharType.DELIMITER;
        }

     // Basic latin, coding interval 0000-007f               
        else if ((c >= 0x0021) && (c <= 0x007e)) { 
        	// 1 byte number
            if ((c >= 0x0030) && (c <= 0x0039)) {
                ct = CharType.NUM;
            } 
         // 1 -byte character
            else if (((c >= 0x0041) && (c <= 0x005a)) || ((c >= 0x0061) && (c <= 0x007a))) {
                ct = CharType.LETTER;
            }

         // Other characters , punctuation may be considered
            else
                ct = CharType.DELIMITER;
        }


     // Latin-1, encoding range 0080-00ff               
        else if ((c >= 0x00a1) && (c <= 0x00ff)) {
            if ((c >= 0x00c0) && (c <= 0x00ff)) {
                ct = CharType.LETTER;
            } else
                ct = CharType.DELIMITER;
        } else
            ct = CharType.OTHER;

        return ct;
    }

    public static void main(String[] args) throws Exception {
        Set<String> pinyins = new LinkedHashSet<String>();
        String raw = "Motorcycle Tour";
        System.out.println("raw: " + raw);
        raw = raw.replaceAll("-", ",");
        String[] raws = raw.split(",");
        for (String item : raws) {
            item = item.trim();
            if (StringUtils.isBlank(item)) {
                continue;
            }
            if (item.length() > 10) {
                item = item.substring(0, 10);
            }
            pinyins.addAll(toPinyin(item, MODE.both));
        }

        for (String py : pinyins) {
            System.out.println(" - " + py);
        }
    }
}