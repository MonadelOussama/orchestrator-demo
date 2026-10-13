package fixtures.inherit;
import com.demo.Inject;
public class BaseClient {
    @Inject protected Dep dep;
    public Dep getDep() { return dep; }
}
