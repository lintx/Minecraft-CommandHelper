package org.lintx.plugins.modules.command_helper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于命令执行的注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandMapping {
    /**
     * 命令路径
     * 以空格分隔的完整命令，如你有命令:/test a b c，则应该写"test a b c"
     * 支持多个路径对应到同一个方法执行，如:{"test a b c","testable"}，那么执行"/test a b c"和"/testable"都能执行这个方法
     * 注解的方法必须是public的，否则无法执行
     * 除第一个空格前面之外，其他部分均支持变量写法：
     * []:可选参数，如："test [a]"，则表示a是可选的参数，输入"/test"和"/test a"都能执行这个方法，以下写法均支持可选
     * {s#[]}:参数绑定且限定值，s可以使用@CommandVariable绑定到方法同名的参数上，如:public a(@CommandVariable String s)
     *          同时，s的值只能为方括号中的值，方括号中的值以逗号分隔，如:"test {s#[a,b,c]}"，则只有输入"/test a",
     *          "/test b" "/test c"才能执行这个方法
     * {s:}:参数绑定且限定值为正则表达式，s为想要绑定的变量名，然后接一个冒号，冒号后面是正则表达式，只要符合正则表达式的要求，
     *          就会执行这个方法，但是使用该方法会导致该段命令无命令提示（因为符合正则表达式的字符太多）
     * {s#p}:参数绑定且限定值为玩家名，s为想要绑定的变量名，然后接一个井号，井号后面是字母p
     * {s@a}:参数绑定且限定值，s为想要绑定的变量名，然后接一个@，@后面是同一个类中的其他方法名，或一个标识，具体使用请参阅@CommandArgsParse
     * {*s}:剩余参数绑定，匹配剩下的所有参数，并进行参数绑定，如："test a *s"，那么在输入"test a b c d"时，会匹配到该方法，且s的值为["b","c","d"]
     */
    String[] path() default {};

    //命令的优先级，越大越高，如果一个命令可以对应多个方法，则从优先级高的开始执行
    int priority() default 100;

    //是否终止命令，如果一个命令可以对应多个方法，那么在执行到本方法时是否终止其他命令的执行
    boolean breakProcess() default true;

    //该命令所需要的权限
    String permission() default "";

    //权限不足时的提示，可以使用多行文本
    String[] permissionInfo() default {"&cI'm sorry, but you do not have permission to perform this command."};

    //是否自动处理权限，值为true，且permission不为空时，如果检测到执行者没有所需权限，将向执行者发送permissionInfo，并终止当前方法
    boolean autoPermission() default false;

    //命令是否必须在控制台执行
    boolean mustConsole() default false;

    //命令必须在控制台执行，但是一个玩家尝试执行命令时的提示
    String[] notConsoleInfo() default {"&cI'm sorry, but you must execute this command in the console."};

    //命令是否必须以玩家身份执行
    boolean mustPlayer() default false;

    //命令必须以玩家身份执行，但是尝试在控制台执行时的提示
    String[] notPlayerInfo() default {"&cI'm sorry, but you must execute this command as a player."};
}
