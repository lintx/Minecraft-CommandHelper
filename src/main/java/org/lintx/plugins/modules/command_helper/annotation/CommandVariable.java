package org.lintx.plugins.modules.command_helper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 参数绑定的注解，用于参数上，如：@CommandVariable String[] args
 * 当方法的参数有该注解时，将尝试进行参数绑定，但是以下类型的参数无需注解也可绑定：
 * 1. CommandSender
 * 2. Command
 * 3. Plugin和插件主类
 * 但是你依然可以手动指定注解以绑定这些类型的变量
 */
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandVariable {
    CommandVariableType type() default CommandVariableType.DEFAULT;
    //由于java编译时默认不会保留方法的参数名，会统一修改为arg0,arg1等，所以需要使用name指定绑定的参数名
    String name() default "";
}
