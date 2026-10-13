package fixtures.setter;
import com.demo.Inject;
public class FieldOnlyClient {
    @Inject private Dep dep;
    public Dep getDep() { return dep; }
}
