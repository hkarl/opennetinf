package netinf.node.resolution.mdht;

import rice.p2p.commonapi.Id;
import rice.p2p.past.ContentHashPastContent;

/**
 * @author PG NetInf 3, University of Paderborn
 */
public class DummyPastContent extends ContentHashPastContent {

   private static final long serialVersionUID = -8331463894587450402L;

   private transient String content;

   public DummyPastContent(Id myId, String content) {
      super(myId);
      this.content = content;
   }

}
