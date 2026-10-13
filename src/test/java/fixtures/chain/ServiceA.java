package fixtures.chain;
import com.demo.Inject;
public class ServiceA {
    @Inject private ServiceB b;
    public ServiceB getB() { return b; }
}
