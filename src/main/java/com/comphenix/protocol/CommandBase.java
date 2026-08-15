package com.comphenix.protocol;

import com.comphenix.protocol.error.ErrorReporter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

abstract class CommandBase implements CommandExecutor {
    public static final com.comphenix.protocol.error.ReportType REPORT_COMMAND_ERROR=new com.comphenix.protocol.error.ReportType("Cannot execute command %s."); public static final com.comphenix.protocol.error.ReportType REPORT_UNEXPECTED_COMMAND=new com.comphenix.protocol.error.ReportType("Incorrect command assigned to %s."); public static final String PERMISSION_ADMIN="protocol.admin";
    private final String permission,name; private final int minimumArgumentCount; protected ErrorReporter reporter;
    public CommandBase(ErrorReporter reporter,String permission,String name){this(reporter,permission,name,0);} public CommandBase(ErrorReporter reporter,String permission,String name,int minimumArgumentCount){this.reporter=reporter;this.permission=permission;this.name=name;this.minimumArgumentCount=minimumArgumentCount;}
    public final boolean onCommand(CommandSender sender,Command command,String label,String[] args){try{if(command==null||!command.getName().equalsIgnoreCase(name))return false;if(permission!=null&&!sender.hasPermission(permission)){sender.sendMessage(ChatColor.RED+"You haven't got permission to run this command.");return true;}return args!=null&&args.length>=minimumArgumentCount&&handleCommand(sender,args);}catch(Throwable error){if(reporter!=null)reporter.reportDetailed(this,"Error executing "+name,error);return true;}}
    protected Boolean parseBoolean(Deque<String> arguments,String parameterName){if(arguments==null||arguments.isEmpty())return null;String value=arguments.peek().toLowerCase();Boolean result=value.equals("true")||value.equals("on")||value.equalsIgnoreCase(parameterName)?Boolean.TRUE:value.equals("false")||value.equals("off")?Boolean.FALSE:null;if(result!=null)arguments.poll();return result;}
    protected Deque<String> toQueue(String[] args,int start){return new ArrayDeque<>(Arrays.asList(args).subList(start,args.length));} public String getPermission(){return permission;} public String getName(){return name;} protected ErrorReporter getReporter(){return reporter;} protected abstract boolean handleCommand(CommandSender sender,String[] args);
}
