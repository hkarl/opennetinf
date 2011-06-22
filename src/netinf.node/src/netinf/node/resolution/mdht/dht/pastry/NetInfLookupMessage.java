package netinf.node.resolution.mdht.dht.pastry;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.past.messaging.LookupMessage;

public class NetInfLookupMessage extends LookupMessage {


	private static final long serialVersionUID = 6146689282945987009L;
	private int level;
	
	public NetInfLookupMessage(int uid, Id id, NodeHandle source, Id dest, int level) {
		super(uid, id, source, dest);
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

}
