package fixtures.circular;
import com.demo.Inject;
public class Ping {
    @Inject private Pong pong;
    public Pong getPong() { return pong; }
}
