package org.lintx.plugins.modules.command_helper.command;

import org.lintx.plugins.modules.command_helper.annotation.CommandMapping;
import org.lintx.plugins.modules.command_helper.annotation.CommandVariable;
import org.lintx.plugins.modules.command_helper.annotation.CommandVariableType;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public abstract class CommandContext implements Cloneable {
    protected Map<String,String> params = new HashMap<>();
    protected List<String> path = new ArrayList<>();
    protected List<String> players = new ArrayList<>();

    abstract protected boolean senderHasPermission(CommandMapping mapping);
    abstract protected boolean senderIsConsole();
    abstract protected boolean senderIsPlayer();
    abstract protected void sendMessageToSender(String[] message);
    abstract protected Object structureArgs(CommandVariableType commandVariableType, CommandMapping mapping,Class<?> cla,Type type,String name);

    protected Object[] structureArgs(Method method, CommandMapping mapping){
        Parameter[] parameters = method.getParameters();
        Object[] result = new Object[parameters.length];
        for (int i = 0;i<parameters.length;i++){
            Parameter parameter = parameters[i];
            CommandVariable variable = parameter.getAnnotation(CommandVariable.class);
            CommandVariableType type = CommandVariableType.DEFAULT;
            String name = "";
            boolean mark = false;
            if (variable!=null){
                type = variable.type();
                name = variable.name();
                mark = true;
            }
            Class<?> t = parameter.getType();
            Type ct = method.getGenericParameterTypes()[i];
            switch (type){
                case SENDER:
                case COMMAND:
                case LABEL:
                case PLUGIN:
                case HAS_PERMISSION:
                    result[i] = structureArgs(type,mapping,t,ct,name);
                    break;
                case PERMISSION:
                    result[i] = t==String.class ? mapping.permission() : defaultData(t);
                    break;
                case VARIABLE:
                    result[i] = parseData(t,method.getGenericParameterTypes()[i],params.get(name));
                    break;
                case DEFAULT:
                    if (!mark){
                        result[i] = defaultData(t);
                    }else {
                        result[i] = structureArgs(type,mapping,t,ct,name);
                    }
                    break;
                default:
                    result[i] = defaultData(t);
            }
        }
        return result;
    }

    void setValue(String key, String value) {
        if (!"".equals(key)) params.put(key,value);
    }

    @SuppressWarnings("unused")
    String getValue(String key) {
        String value = params.get(key);
        if (value==null) value = "";
        return value;
    }

    String firstPath(){
        if (path.size()>0) return path.remove(0);
        return "";
    }

    String allPath(){
        String result = String.join(" ",path);
        path.clear();
        return result;
    }

    void reductionPath(String first){
        path.add(0,first);
    }

    boolean pathIsEnd() {
        return path.size()==0;
    }

    List<String> players() {
        return players;
    }

    public CommandContext clone(){
        CommandContext context = null;
        try {
            context = (CommandContext) super.clone();
            context.path = new ArrayList<>(path);
            context.params = new HashMap<>(params);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return context;
    }

    protected Object defaultData(Class<?> type){
        return parseData(type,null,"");
    }

    protected Object parseData(Class<?> cla, Type type, String value){
        String ls = value.toLowerCase(Locale.ROOT);
        if (cla==byte.class){
            return 0;
        }else if (cla==short.class){
            return 0;
        }else if (cla==int.class || cla==Integer.class){
            try {
                return Integer.parseInt(ls);
            }catch (NumberFormatException ignored){
                return 0;
            }
        }else if (cla==long.class){
            return 0;
        }else if (cla==float.class || cla==Float.class){
            try {
                return Float.parseFloat(ls);
            }catch (NumberFormatException ignored){
                return .0f;
            }
        }else if (cla==double.class){
            return .0d;
        }else if (cla==boolean.class || cla==Boolean.class){
            return  "true".equals(ls) || "yes".equals(ls);
        }else if (cla==char.class){
            return Character.MIN_VALUE;
        }else if (cla==String.class){
            return "";
        }else if (cla==String[].class){
            return value.split(" ");
        }else if (cla==List.class){
            if (type instanceof ParameterizedType){
                ParameterizedType pType = (ParameterizedType) type;
                if (pType.getRawType() == List.class){
                    if (pType.getActualTypeArguments().length==1){
                        Type first = pType.getActualTypeArguments()[0];
                        if (first == String.class){
                            return new ArrayList<>(Arrays.asList(value.split(" ")));
                        }
                    }
                }
            }
            return null;
        }else {
            return null;
        }
    }
}
