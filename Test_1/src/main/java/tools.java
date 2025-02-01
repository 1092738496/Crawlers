import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @time: 2024/3/6 14:16
 * @description:
 */

public class tools {
    public static List<String> regex(String text, String regex) {
        List<String> list = new LinkedList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            list.add(matcher.start() + "," + matcher.end());
        }
        return list;
    }

    public static String regexStr(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        StringBuilder z = new StringBuilder();
        while (matcher.find()) {
            if (!matcher.group().equals("")) {
                z.append(matcher.group());
            }
        }

        return z.toString();
    }


    public Double Re(String value) {
        Double index = null;
        if (!value.equals("")) {
            index = Double.valueOf(value.replaceAll("%", ""));
        }
        return index;
    }


}
