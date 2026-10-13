package fixtures.circular;
import com.demo.Inject;
public class Pong {
    @Inject private Ping ping;
    public Ping getPing() { return ping; }
}
