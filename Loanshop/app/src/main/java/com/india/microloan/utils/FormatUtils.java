package com.india.microloan.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FormatUtils {


    public static Date stringToDate(String str) {
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }




    /**
     * 将yyyy-MM-dd时间改为yyyy年MM月dd日
     * @param date yyyy-MM-dd格式的时间
     * @return
     */
    public static String getDateWithNYRFormaterByLine(String date){
        String newDateDisplay = date;
        Date newDate = FormatUtils.stringToDate(date);
        if (newDate != null){
            SimpleDateFormat newSimpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
            newDateDisplay = newSimpleDateFormat.format(newDate);
        }
        return newDateDisplay;
    }
    /**
     * 格式化钱
     *
     * @param num
     * @return #,###,##0.00
     */
    public static String formatMoney(Double num) {
        if (null == num) {
            return "0.00";
        }
        num = Double.valueOf(Math.round(num * 100)) / 100;
        DecimalFormat Formatter = new DecimalFormat("#,###,##0.00");
        return Formatter.format(num);
    }


    /**
     * 格式化日期
     */
    public static String formatDateTime(Date date) {
        if (null == date)
            return "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setLenient(false);
        return simpleDateFormat.format(date);
    }
    /**
     * 格式化日期
     */
    public static String formatDate(Date date) {
        if (null == date)
            return "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        simpleDateFormat.setLenient(false);
        return simpleDateFormat.format(date);
    }


    public static String formatYearMonth(Date date) {
        if (null == date)
            return "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        simpleDateFormat.setLenient(false);
        return simpleDateFormat.format(date);
    }


    public static String formatYearMonthWithChinese(Date date) {
        if (null == date)
            return "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月");
        simpleDateFormat.setLenient(false);
        return simpleDateFormat.format(date);
    }


    public static Date praseStringToDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}

