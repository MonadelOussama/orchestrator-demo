package fixtures.chain;
import com.demo.Inject;
public class ServiceB {
    @Inject private ServiceC c;
    public ServiceC getC() { return c; }
}
