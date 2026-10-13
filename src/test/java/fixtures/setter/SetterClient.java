package fixtures.setter;
import com.demo.Inject;
public class SetterClient {
    @Inject private Dep dep;
    private boolean setterCalled = false;
    public Dep getDep() { return dep; }
    public void setDep(Dep dep) { this.dep = dep; this.setterCalled = true; }
    public boolean isSetterCalled() { return setterCalled; }
}
