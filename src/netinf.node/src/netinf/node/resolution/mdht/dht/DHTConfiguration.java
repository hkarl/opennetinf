package netinf.node.resolution.mdht.dht;

public class DHTConfiguration {
   
   private String bootHost;
   private int bootPort;
   private int listenPort;
   private int level;

   public DHTConfiguration(String bootHost, int bootPort, int listenPort, int level) {
      this.bootHost = bootHost;
      this.bootPort = bootPort;
      this.listenPort = listenPort;
   }

   public String getBootHost() {
      return bootHost;
   }

   public int getBootPort() {
      return bootPort;
   }

   public int getListenPort() {
      return listenPort;
   }
   
   public int getLevel() {
      return level;
   }

}
