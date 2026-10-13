package fixtures.shared;
import com.demo.Inject;
public class ClientTwo {
    @Inject private Shared shared;
    public Shared getShared() { return shared; }
}
