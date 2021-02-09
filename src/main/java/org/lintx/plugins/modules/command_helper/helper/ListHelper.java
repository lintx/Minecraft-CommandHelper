package org.lintx.plugins.modules.command_helper.helper;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ListHelper {
    public static boolean returnIsList(Method method){
        if (method.getReturnType().equals(String[].class)) return true;
        Type type = method.getGenericReturnType();
        if (!(type instanceof ParameterizedType)) return false;
        ParameterizedType tape = (ParameterizedType) type;
        if (tape.getRawType() != List.class) return false;
        if (tape.getActualTypeArguments().length!=1) return false;
        Type first = tape.getActualTypeArguments()[0];
        return first == String.class;
    }

    public static List<String> parseReturn(Method method,Object result,boolean lower){
        List<String> list = new ArrayList<>();
        if (method.getReturnType().equals(String[].class)) return Arrays.asList((String[])result);
        Type type = method.getGenericReturnType();
        if (!(type instanceof ParameterizedType)) return list;
        ParameterizedType pate = (ParameterizedType) type;
        if (pate.getRawType() != List.class) return list;
        if (pate.getActualTypeArguments().length!=1) return list;
        Type first = pate.getActualTypeArguments()[0];
        if (first == String.class){
            for (Object o : (List<?>)result){
                if (o instanceof String){
                    String r = (String) o;
                    if (lower) r = r.toLowerCase(Locale.ROOT);
                    list.add(r);
                }
            }
        }
        return list;
    }
}
