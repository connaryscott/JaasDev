package sample;

public class Log {

   private String className;
   private boolean debug = false;

   public Log(Object object, boolean debug) {
      this.className = object.getClass().getName();
      this.debug = debug;
   }

   public void debug(String msg) {
      if (debug) 
         System.out.println(className + " DEBUG: " + msg);
   }
   public void info(String msg) {
       System.out.println(className + " INFO: " + msg);
   }
   public void warn(String msg) {
       System.err.println(className + " WARN: " + msg);
   }
   public void error(String msg) {
       System.err.println(className + " ERROR: " + msg);
   }

}
