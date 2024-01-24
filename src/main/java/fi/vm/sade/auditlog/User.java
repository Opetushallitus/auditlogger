package fi.vm.sade.auditlog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.ietf.jgss.Oid;

import java.net.InetAddress;

public class User {
    private final Oid oid;
    private final InetAddress ip;
    private final String session;
    private final String userAgent;
    private final ObjectMapper mapper = new ObjectMapper();

    public User(InetAddress ip, String session, String userAgent) {
        this(null, ip, session, userAgent);
    }
    
    public User(Oid oid, InetAddress ip, String session, String userAgent) {
        this.oid = oid;
        this.ip = ip;
        this.session = session;
        this.userAgent = userAgent;
    }

    public ObjectNode asJson() {
        ObjectNode o = mapper.createObjectNode();
        if (oid != null) {
            o.put("oid", oid.toString());
        }
        o.put("ip", ip.getHostAddress());
        o.put("session", session);
        o.put("userAgent", userAgent);
        return o;
    }
}
