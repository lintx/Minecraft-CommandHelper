package org.lintx.plugins.modules.command_helper.annotation;

/**
 * 参数绑定的类型
 */
public enum CommandVariableType {
    /**
     * 默认类型，会尝试自动解析参数的类型和名称来绑定对应的参数，如：
     * 参数类型为CommandSender时会绑定执行的CommandSender
     * 参数类型为Command时会绑定执行的Command
     * 参数类型为Plugin或插件主类时会绑定插件主类
     * 参数类型为String且name为label且命令中没有绑定label变量时会绑定label变量（插件名）
     * 参数类型为String[]且name为args且命令中没有绑定args时会绑定原生命令的args参数（即所有参数）
     * 参数类型为String且name为permission且命令中没有绑定permission变量时会绑定permission变量（所需权限）
     * 参数类型为boolean且name为hasPermission且命令中没有绑定hasPermission变量时会绑定hasPermission变量（是否具有所需权限）
     * 不在上述列表中的，将尝试绑定命令中的同名变量，支持的数据类型有：
     * String,Integer,Boolean,Float,int,boolean,float,Player(bukkit),String[],List<String>
     */
    DEFAULT,

    //将绑定CommandSender
    SENDER,

    //将绑定Command
    COMMAND,

    //将绑定label
    LABEL,

    //将绑定插件主类
    PLUGIN,

    //将绑定注解中所写的权限
    PERMISSION,

    //将绑定该角色是否具有所需的权限
    HAS_PERMISSION,

    //将绑定命令中的同名变量
    VARIABLE
}
