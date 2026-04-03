package com.SCAUteam11.GYJZ.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import java.util.Random;

public class UsernameGenerator {
    private static final Random RANDOM = new Random();

    /**
     * 生成机构专属账号
     * 规则：机构拼音首字母缩写(4-6位) + 下划线 + 4位随机数字
     */
    public static String generateAccount(String orgName) {
        if (orgName == null || orgName.trim().isEmpty()) {
            throw new IllegalArgumentException("机构名称不能为空");
        }

        // 1. 获取机构名拼音缩写
        String pinyinAbbr = getPinyinAbbreviation(orgName);

        // 2. 生成随机后缀（4位数字或字母）
        String suffix = generateRandomSuffix(4);

        // 3. 组合账号
        String account = pinyinAbbr + "_" + suffix;

        // 4. 转为小写（统一规范）
        return account.toLowerCase();
    }

    /**
     * 获取拼音缩写（取每个字的首字母）
     */
    private static String getPinyinAbbreviation(String chinese) {
        StringBuilder abbr = new StringBuilder();
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        for (int i = 0; i < chinese.length() && abbr.length() < 8; i++) {
            char c = chinese.charAt(i);

            // 英文字母直接保留
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                abbr.append(Character.toLowerCase(c));
            }
            // 中文字符转拼音首字母
            else if (c >= 0x4E00 && c <= 0x9FA5) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        abbr.append(pinyinArray[0].charAt(0));
                    }
                } catch (Exception e) {
                    // 转换失败，跳过
                }
            }
        }

        // 确保至少4位，不足则补上默认值
        String result = abbr.toString();
        if (result.length() < 4) {
            result = result + "org";
        }

        return result.length() > 8 ? result.substring(0, 8) : result;
    }

    /**
     * 生成随机后缀（数字+字母）
     */
    private static String generateRandomSuffix(int length) {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 带唯一性校验的生成（防止重复）
     */
    public static String generateUniqueAccount(String orgName, AccountValidator validator) {
        int maxRetries = 5;
        for (int i = 0; i < maxRetries; i++) {
            String account = generateAccount(orgName);
            // 如果账号已存在，重新生成（加随机后缀）
            if (validator.isAccountAvailable(account)) {
                return account;
            }
        }
        // 重试失败，使用时间戳方案
        return getPinyinAbbreviation(orgName) + "_" + System.currentTimeMillis() % 10000;
    }

    // 校验接口
    public interface AccountValidator {
        boolean isAccountAvailable(String account);
    }
}
