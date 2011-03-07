/**
 * 
 */
package netinf.node.resolution.mdht;

import rice.p2p.commonapi.Id;
import rice.p2p.past.ContentHashPastContent;

/**
 * @author razvan
 *
 */
@SuppressWarnings("serial")
public class DummyPastContent extends ContentHashPastContent {
	
	transient String content; 

	public DummyPastContent(Id myId, String content) {
		super(myId);
		this.content = content;
	}

	
}
