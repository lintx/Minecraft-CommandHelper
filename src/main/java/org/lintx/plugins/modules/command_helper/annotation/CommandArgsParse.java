package org.lintx.plugins.modules.command_helper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandArgsParse {
    /**
     * 当一个命令的path中包含{s@a}节点，在执行命令时，将会尝试在命令所在类查找所有包含该注解的方法
     * 然后匹配name和为@后面的字符（本例中为a）的方法，无法匹配时将匹配方法名为@后面的字符的方法
     * 如果匹配到对应的方法，那么该节点允许的值只能是该方法返回的值之一
     * 该注解对应的方法返回值必须是String[]或List<String>
     */
    String name() default "";
}
