package com.nix.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;


/**
 * SQL工具类
 * 
 * @author Ming
 *
 */
public class SQLUtil {
	/**
	 * 格式化字段
	 * 
	 * @param parameters 字段
	 * @return 格式化结果
	 */
	public static String formatParameters(String[] parameters) {
		if (parameters==null) {
			return null;
		}
		StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append(parameters[0]);
		for (int i = 1, length = parameters.length; i < length; i++) {
			stringBuilder.append("," + parameters[i]);
		}
		
		return stringBuilder.toString();
	}
	
	/**
	 * 条件SQL参数填充
	 * @param condition 条件
	 * @param values 参数
	 * @return 条件SQL
	 */
	@SuppressWarnings("deprecation")
	public static String fillCondition(String condition, Object ... values) {
		if (condition == null) {
			return null;
		}
		
		StringBuilder stringBuilder = new StringBuilder(condition);
		
		for (int i = 0, length = values.length; i < length; i++) {
			int index = stringBuilder.indexOf("?");
			Object value = values[i];
			String param;
			if (value instanceof String) {
				param = "'" + ((String) value).replaceAll("'", "''") + "'";
			} else if (value instanceof Date) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				param = "'" + simpleDateFormat.format((Date) value) + "'";
			} else if (value instanceof java.sql.Date) {
				param = "'" + ((java.sql.Date) value).toLocaleString() + "'";
			} else {
				param = value.toString();
			}
			stringBuilder.replace(index, index + 1, param);
		}
		
		return stringBuilder.toString();
	}

	public static String sqlFormat(String condition, Object ... values) {
		if (!condition.matches(".*\\?.*")) {
			return condition;
		}
		String value;
		if (values[0] instanceof Date) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			value = simpleDateFormat.format((Date) values[0]);
		} else if (values[0] instanceof java.sql.Date) {
			value = ((java.sql.Date) values[0]).toLocaleString();
		} else {
			value = values[0].toString();
		}
		return sqlFormat(condition.replaceFirst("\\?",value),Arrays.copyOfRange(values,1,values.length));
	}
	
	public static Integer getOffset(Integer curPage, Integer limit) {
		if (curPage == null || limit < 1 || curPage == null || curPage < 0) {
			return null;
		}
		return (curPage - 1) * limit;
	}
	public static String stringToStringgroup(String targetString) {
		if (targetString==null) {
			return null;
		}
		StringBuffer endString=new StringBuffer("%");
		String string=targetString.replaceAll(" ", "%");
		endString.append(string+"%");
		return endString.toString();
	}

	
	public static void main(String[] args) throws ParseException {
		
		Date date=new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		date=sdf.parse("1995-02-04");
		System.out.println(date.toString());
		
		
		String[] parameters = new String[] {"username", "password"};
		System.out.println(formatParameters(parameters));
		
		
		
		String condition = "username = '?' and sex = '?' and create_date = '?' and update_date = '?'";
		Object[] values = new Object[] {"Ming", 1, new Date(), new java.sql.Date(System.currentTimeMillis())};
		System.out.println(sqlFormat(condition, values));
	}
}
