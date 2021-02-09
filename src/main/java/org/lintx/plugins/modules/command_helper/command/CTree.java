package org.lintx.plugins.modules.command_helper.command;

import org.lintx.plugins.modules.command_helper.annotation.CommandArgsParse;
import org.lintx.plugins.modules.command_helper.annotation.CommandHelper;
import org.lintx.plugins.modules.command_helper.annotation.CommandMapping;
import org.lintx.plugins.modules.command_helper.helper.ListHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CTree implements Comparable<CTree> {
    private Logger logger;
    private boolean root = false;
    private final List<CTree> children = new ArrayList<>();   //子路由
    private String path = "";//当前的路径，比如{sap}
    private Object object = null;
    private boolean optional = false;//当前路径是否可选
    private CTreeType type = CTreeType.DEFINE;  //路径类型
    private String parameterName = "";  //参数绑定的名字
    private Method argMethod = null;    //允许的值从其他method获取
    private final List<String> allowList = new ArrayList<>(); //允许的list，一般对应{s#[]}
    private final List<String> lowAllowList = new ArrayList<>(); //允许的list，一般对应{s#[]}
    private String word = "";   //具体的word，一般对应固定字符串
    private String regexp = ""; //正则
    private Method method = null;   //最终执行的方法
    CommandMapping mapping;
    private CommandContext context = null;

    private CTree(){

    }

    public static CTree newRoot(Logger logger){
        CTree tree = new CTree();
        tree.root = true;
        tree.logger = logger;
        return tree;
    }

    public void loadTree(Class<?> clz,Object obj){
        if (!root) return;
        CommandHelper helperAnnotation = clz.getAnnotation(CommandHelper.class);
        if (helperAnnotation!=null){
            for (Method method : clz.getMethods()) {
                CommandMapping mappingAnnotation = method.getAnnotation(CommandMapping.class);
                if (mappingAnnotation!=null){
                    List<String> paths = new ArrayList<>();
                    if (helperAnnotation.path().length>0){
                        for(String p: helperAnnotation.path()){
                            for (String c: mappingAnnotation.path()){
                                paths.add(p + c);
                            }
                        }
                    }else {
                        paths.addAll(Arrays.asList(mappingAnnotation.path()));
                    }
                    for (String path:paths){
                        List<String> parsePath = Arrays.asList(path.split(" "));
                        if (parsePath.size()>0){
                            if (parsePath.get(0).contains(":")){
                                logger.warning("Could not load command " + parsePath.get(0) + ": Illegal Characters");
                                continue;
                            }
                            logger.fine("load mapping ok:" + path);
                            loadTree(parsePath,obj,method,mappingAnnotation);
                        }else {
                            logger.warning("invalid path:'" + path + "'");
                        }
                    }
                }
            }
        }
    }

    private void loadTree(List<String> path,Object obj,Method method,CommandMapping mapping){
        if (path.size()>0){
            String first = path.remove(0);
            boolean isLoad = false;
            for (CTree child:children){
                if (first.equals(child.path)){
                    isLoad = true;
                    child.loadTree(path,obj,method,mapping);
                    break;
                }
            }
            if (!isLoad){
                CTree child = new CTree();
                child.logger = logger;
                child.path = first;
                child.object = obj;
                child.method = method;
                child.mapping = mapping;
                if (first.startsWith("[") && first.endsWith("]")){
                    //可选参数，如[a],[b]等
                    first = first.substring(1,first.length()-1).trim();
                    child.optional = true;
                    logger.fine("mapping optional path:" + path);
                }
                if (first.startsWith("{") && first.endsWith("}")){
                    first = first.substring(1,first.length()-1).trim();
                    if (first.startsWith("*")){
                        //剩余参数绑定，如{*a}等
                        first = first.substring(1).trim();
                        child.type = CTreeType.ALL;
                        child.parameterName = first;
                        logger.fine("mapping all path,name:" + first);
                    }else if (first.contains(":")){
                        //正则表达式，如{a:w}
                        String[] arr = first.split(":",2);
                        child.parameterName = arr[0].trim();
                        child.type = CTreeType.REGEXP;
                        child.regexp = arr[1].trim();
                        logger.fine("mapping regexp path,name:" + child.parameterName + ",regexp:" + child.regexp);
                    } else if (first.contains("@")) {
                        //匹配方法，如{a@method}
                        String[] arr = first.split("@",2);
                        child.parameterName = arr[0].trim();
                        child.type = CTreeType.METHOD;
                        String m = arr[1].trim();
                        Method findMethod = null;
                        for (Method t : obj.getClass().getMethods()) {
                            if (!ListHelper.returnIsList(t)) continue;
                            CommandArgsParse parseAnnotation = t.getAnnotation(CommandArgsParse.class);
                            if (parseAnnotation!=null && parseAnnotation.name().equals(m)){
                                child.argMethod = t;
                                break;
                            }
                            if (t.getName().equals(m)){
                                findMethod = t;
                            }
                        }
                        if (child.argMethod==null && findMethod!=null){
                            child.argMethod = findMethod;
                        }
                        if (child.argMethod==null){
                            logger.warning("Could not load command path " + arr[1] + ": Variable resolution method not found");
                            return;
                        }
                        logger.fine("mapping method path,name:" + child.parameterName + ",method:" + obj.getClass().getName() + ":" + argMethod.getName());
                    }else if (first.contains("#")){
                        String[] arr = first.split("#",2);
                        child.parameterName = arr[0].trim();
                        if ("p".equals(arr[1])){
                            //对应玩家列表，如{s#p}
                            child.type = CTreeType.PLAYER;
                            logger.fine("mapping player path,name:" + child.parameterName);
                        }else {
                            if (arr[1].startsWith("[") && arr[1].endsWith("]")){
                                //对应固定参数，如{s#[a,b,c]}
                                child.type = CTreeType.LIST;
                                String l = arr[1].substring(1,arr[1].length()-1).trim();
                                for (String s:l.split(",")){
                                    child.allowList.add(s.trim());
                                    child.lowAllowList.add(s.trim().toLowerCase(Locale.ROOT));
                                }
                                logger.fine("mapping list path,name:" + child.parameterName + ",list:" + String.join(",",child.allowList));
                            }else {
                                logger.warning("Could not load command path " + arr[1] + ": Illegal Characters");
                                return;
                            }
                        }
                    }
                }else {
                    //固定值，如a,b等
                    child.word = first.toLowerCase(Locale.ROOT);
                    logger.fine("mapping define path,path:" + first);
                }
                children.add(child);
                child.loadTree(path,obj,method,mapping);
            }
        }
    }

    public Map<String, Map<String, Object>> getCommandMap(){
        Map<String, Map<String, Object>> map = new HashMap<>();
        if (!root) return map;
        for (CTree child : children) {
            if (!"".equals(child.path)){
                map.put(child.path,new HashMap<>());
            }
        }
        return map;
    }

    public boolean onCommand(CommandContext context){
        if (!root) return false;
        List<CTree> trees = commandExecutor(context);
        if (trees==null) {
            return false;
        }
        Collections.sort(trees);
        for (CTree tree:trees){
            if (tree==null) continue;
            tree.commandExecutor();
            if (tree.mapping.breakProcess()) break;
        }
        return true;
    }

    private void commandExecutor(){
        logger.fine("executor command:" + object.getClass() + ":" + method.getName());
        if (context==null) {
            logger.fine("context is null!");
            return;
        }
        if (method==null) {
            logger.fine("method is null!");
            this.context = null;
            return;
        }
        if (mapping==null) {
            logger.fine("mapping is null!");
            this.context = null;
            return;
        }
        if (mapping.autoPermission() && !context.senderHasPermission(mapping)){
            logger.fine("sender not has permission!");
            context.sendMessageToSender(mapping.permissionInfo());
            this.context = null;
            return;
        }
        if (mapping.mustConsole() && !context.senderIsConsole()){
            logger.fine("sender not a console!");
            context.sendMessageToSender(mapping.notConsoleInfo());
            this.context = null;
            return;
        }
        if (mapping.mustPlayer() && !context.senderIsPlayer()){
            logger.fine("sender not a player!");
            context.sendMessageToSender(mapping.notPlayerInfo());
            this.context = null;
            return;
        }
        invoke(method,context);
        this.context = null;
    }

    private List<CTree> commandExecutor(CommandContext context){
        boolean isMatch = false;
        if (!root){
            String first = type==CTreeType.ALL ? context.allPath() : context.firstPath();
            String find = first.toLowerCase(Locale.ROOT);
            isMatch = isMatch(context,find);
            logger.fine("match node,first:" + find + ",path:" + path + "," + (isMatch?"match":"not match"));
            if (isMatch){
                if (!"".equals(parameterName)) {
                    logger.fine("parameter name:" + parameterName + ",value:" + first);
                    context.setValue(parameterName,first);
                }
                if (children.size()==0){
                    //是最后一级
                    List<CTree> trees = new ArrayList<>();
                    this.context = context;
                    trees.add(this);
                    return trees;
                }
            }else if (optional){
                context.reductionPath(first);
                if (children.size()==0){
                    //是最后一级
                    return null;
                }
            }else {
                //既没匹配到，又不是可选，直接终止这条线路
                return new ArrayList<>();
            }
        }
        //肯定不是最后一级
        List<CTree> trees = new ArrayList<>();
        for (CTree tree:children){
            List<CTree> result = tree.commandExecutor(context.clone());
            if (result==null){
                //下级没匹配到，但是是可选的
                if (isMatch){
                    if (!trees.contains(this)) {
                        this.context = context;
                        trees.add(this);
                    }
                }else {
                    if (!trees.contains(null)) trees.add(null);
                }
            }else {
                trees.addAll(result);
            }
        }
        if (trees.size()==0){
            //下级都没有匹配到，而且下级不是可选的
            return trees;
        }else if (trees.size()==1 && trees.get(0)==null){
            //所有下级返回的都是null
            if (isMatch){
                //本级匹配到了，返回本级
                this.context = context;
                trees.add(this);
                return trees;
            }else {
                //本级没有匹配到，那么因为既没有匹配到又不是可选的情况不会往下寻找，所以本级必然是可选的，继续往上传递
                return null;
            }
        }else if (trees.contains(null) && isMatch){
            //至少一个下级返回的是null，并且本级匹配到了
            //如果有下级返回了null，但是本级也是可选的，则把这个null向上传递，直到处理的级别不是可选的
            trees.removeIf(Objects::isNull);
            if (!trees.contains(this)) {
                this.context = context;
                trees.add(this);
            }
        }
        //剩余的情况有：
        //1.size是1，但是不是null，比如是某个下级节点，或者是本类
        //2.size大于1，含有null，本类可选，把null向上传递
        //3.size大于1，不含null，正常list
        return trees;
    }

    public List<String> onTabComplete(CommandContext context){
        List<String> result = new ArrayList<>();
        if (!root){
            String first = type==CTreeType.ALL ? context.allPath() : context.firstPath();
            String find = first.toLowerCase(Locale.ROOT);
            if (context.pathIsEnd()){
                //如果命令已经结束，那么寻找所有可能的提示，并返回以最后一节开头的提示
                List<String> mays = mayWord();
                for (String may : mays){
                    if (find.startsWith(may.toLowerCase(Locale.ROOT))) result.add(may);
                }
                return result;
            }else if (isMatch(context,find)){
                //如果匹配到本节点，那么给context设置变量，不返回
                if (!"".equals(parameterName)) context.setValue(parameterName,first);
            }else if (optional){
                //如果本节点是可选，而且没有匹配到本节点，那么把first还给path，不返回
                context.reductionPath(first);
            }else {
                //既没有匹配到本节点，又不是可选，那么就不再继续往下查找
                return result;
            }
        }
        for (CTree tree : children){
            //如果匹配到了本节点，或者本节点是可选的，那么继续往下匹配
            result.addAll(tree.onTabComplete(context.clone()));
        }
        return result.stream().distinct().collect(Collectors.toList());
    }

    private boolean isMatch(CommandContext context,String find){
        switch (type){
            case LIST:
                return lowAllowList.contains(find);
            case DEFINE:
                return word.equals(find);
            case PLAYER:
            case ALL:
                return true;
            case METHOD:
                return ListHelper.parseReturn(argMethod,invoke(argMethod,context),true).contains(find);
            case REGEXP:
                return Pattern.matches(regexp,find);
        }
        return false;
    }

    private List<String> mayWord(){
        List<String> list = new ArrayList<>();
        switch (type){
            case LIST:
                list.addAll(allowList);
                break;
            case DEFINE:
                list.add(path);
                break;
            case PLAYER:
                list.addAll(context.players());
                break;
            case METHOD:
                return ListHelper.parseReturn(argMethod,invoke(argMethod,context),false);
        }
        if (optional){
            for (CTree child : children) {
                list.addAll(child.mayWord());
            }
        }
        return list;
    }

    private Object invoke(Method method,CommandContext context){
        try {
            Object[] args = context.structureArgs(method,mapping);
            return method.invoke(object,args);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int compareTo(@Nullable CTree o) {
        if (o==null) return 1;
        if (mapping==null || o.mapping==null) return 1;
        if (context.pathIsEnd() && !o.context.pathIsEnd()) return -1;
        if (o.context.pathIsEnd() && !context.pathIsEnd()) return 1;
        return Integer.compare(o.mapping.priority(), mapping.priority());
    }
}
