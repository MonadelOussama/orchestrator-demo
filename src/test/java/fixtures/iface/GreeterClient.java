package fixtures.iface;
import com.demo.Inject;
public class GreeterClient {
    @Inject private Greeter greeter;
    public Greeter getGreeter() { return greeter; }
}
