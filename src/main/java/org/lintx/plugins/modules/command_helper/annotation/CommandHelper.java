package org.lintx.plugins.modules.command_helper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 所有的需要handle的类都需要在类上使用该注解
 * 被注解的类必须有以下构造方法之一才能被自动初始化，否则需要手动初始化后手动add（以下所有构造方法都是public的）
 * 1. 带有一个参数，且参数类型为插件主类的（即，和new CommandHelper时的参数一致）构造方法
 * 2. 不带任何参数的构造方法
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandHelper {
    //path注解的写法同CommandMapping的path，如果类有该注解，则视为所有Mapping的公共前置，比如类的值为"a b"，方法的值为"c d"，则需要键入命令"/a b c d"
    String[] path() default {};
}
