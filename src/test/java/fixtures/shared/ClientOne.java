package fixtures.shared;
import com.demo.Inject;
public class ClientOne {
    @Inject private Shared shared;
    public Shared getShared() { return shared; }
}
