package netinf.node.resolution.mdht.dht.pastry;

import java.net.InetAddress;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.past.PastContent;
import rice.p2p.past.messaging.InsertMessage;

public class NetInfInsertMessage extends InsertMessage {

	private static final long serialVersionUID = 283952122270522339L;
	private InetAddress sourceAddr;
	private int level;
	private int maxlevels;
	public NetInfInsertMessage(int uid, PastContent content, NodeHandle source,
			Id dest, InetAddress sAddr, int level, int maxLevels) {
		super(uid, content, source, dest);
		this.sourceAddr = sAddr;
		this.level = level;
		this.maxlevels = maxLevels;
	}
	
	public InetAddress getAddress() {
		return this.sourceAddr;
	}
	
	public int getLevel(){
		return this.level;
	}
	
	public int getMaxLevels(){
		return this.maxlevels;
	}

}
