package com.demo;

import fixtures.ambiguous.ShapeClient;
import fixtures.chain.ServiceA;
import fixtures.chain.ServiceB;
import fixtures.chain.ServiceC;
import fixtures.circular.Ping;
import fixtures.circular.Pong;
import fixtures.iface.EnglishGreeter;
import fixtures.iface.Greeter;
import fixtures.iface.GreeterClient;
import fixtures.inherit.ChildClient;
import fixtures.missing.Car;
import fixtures.nodefault.NoDefault;
import fixtures.plain.PlainClient;
import fixtures.setter.FieldOnlyClient;
import fixtures.setter.SetterClient;
import fixtures.shared.ClientOne;
import fixtures.shared.ClientTwo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrchestratorTest {

    // ---------- basic scenario ----------

    @Test
    void injectsTypeAIntoClientA() throws Exception {
        Orchestrator o = new Orchestrator("com.demo");
        ClientA client = o.getBean(ClientA.class);
        assertNotNull(client.getObjectA(), "objectA should have been injected");
        assertInstanceOf(TypeA.class, client.getObjectA());
    }

    @Test
    void callObjAReturnsHelloFromInjectedObject() throws Exception {
        Orchestrator o = new Orchestrator("com.demo");
        assertEquals("objA hello", o.getBean(ClientA.class).callObjA());
    }

    @Test
    void runWiresBeansWithoutExplicitGetBean() throws Exception {
        Orchestrator o = new Orchestrator("com.demo");
        o.run();
        ClientA client = o.getBean(ClientA.class);
        assertNotNull(client.getObjectA());
    }

    // ---------- scopes ----------

    @Test
    void getBeanReturnsTheSameInstanceEveryTime() throws Exception {
        Orchestrator o = new Orchestrator("com.demo");
        assertSame(o.getBean(ClientA.class), o.getBean(ClientA.class));
    }

    @Test
    void twoClientsShareTheSameDependencyInstance() throws Exception {
        Orchestrator o = new Orchestrator("fixtures.shared");
        o.run();
        ClientOne one = o.getBean(ClientOne.class);
        ClientTwo two = o.getBean(ClientTwo.class);
        assertNotNull(one.getShared());
        assertSame(one.getShared(), two.getShared());
    }

    @Test
    void separateOrchestratorsHaveSeparateContainers() throws Exception {
        Orchestrator o1 = new Orchestrator("com.demo");
        Orchestrator o2 = new Orchestrator("com.demo");
        assertNotSame(o1.getBean(ClientA.class), o2.getBean(ClientA.class));
    }

    // ---------- dependency graphs ----------

    @Test
    void injectsTransitiveDependencies() throws Exception {
        Orchestrator o = new Orchestrator("fixtures.chain");
        ServiceA a = o.getBean(ServiceA.class);
        ServiceB b = a.getB();
        assertNotNull(b, "A -> B");
        ServiceC c = b.getC();
        assertNotNull(c, "B -> C");
    }

    @Test
    void handlesCircularDependencies() throws Exception {
        Orchestrator o = new Orchestrator("fixtures.circular");
        Ping ping = o.getBean(Ping.class);
        Pong pong = ping.getPong();
        assertNotNull(pong);
        assertSame(ping, pong.getPing(), "the cycle must close on the same instances");
    }

    // ---------- interfaces ----------

    @Test
    void resolvesInterfaceToItsSingleImplementation() throws Exception {
        Orchestrator o = new Orchestrator("fixtures.iface");
        GreeterClient client = o.getBean(GreeterClient.class);
        Greeter greeter = client.getGreeter();
        assertInstanceOf(EnglishGreeter.class, greeter);
        assertEquals("hello", greeter.greet());
    }

    @Test
    void throwsWhenSeveralImplementationsExist() throws Exception {
        Orchestrator o = new Orchestrator("fixtures.ambiguous");
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> o.getBean(ShapeClient.class));
        assertTrue(ex.getMessage().contains("Ambiguous"));
    }

    @Test
    void throwsWhenNoImplementationExists() throws Exception {
        Orchestrator o = new Orchestrator("fixtures.missing");
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> o.getBean(Car.class));
        assertTrue(ex.getMessage().contains("No implementation found"));
    }

    // ---------- injection mechanism ----------

    @Test
    void usesTheSetterWhenOneExists() throws Exception {
        Orchestrator o = new Orchestrator("fixtures.setter");
        SetterClient client = o.getBean(SetterClient.class);
        assertNotNull(client.getDep());
        assertTrue(client.isSetterCalled(), "setDep() should have been used");
    }

    @Test
    void fallsBackToDirectFieldAccessWithoutSetter() throws Exception {
        Orchestrator o = new Orchestrator("fixtures.setter");
        FieldOnlyClient client = o.getBean(FieldOnlyClient.class);
        assertNotNull(client.getDep());
    }

    @Test
    void injectsFieldsDeclaredInSuperclass() throws Exception {
        Orchestrator o = new Orchestrator("fixtures.inherit");
        ChildClient child = o.getBean(ChildClient.class);
        assertNotNull(child.getDep());
    }

    @Test
    void leavesFieldsWithoutAnnotationUntouched() throws Exception {
        Orchestrator o = new Orchestrator("fixtures.plain");
        o.run();
        assertNull(o.getBean(PlainClient.class).getDep());
    }

    // ---------- error and edge cases ----------

    @Test
    void failsWhenClassHasNoDefaultConstructor() throws Exception {
        Orchestrator o = new Orchestrator("fixtures.nodefault");
        assertThrows(NoSuchMethodException.class, () -> o.getBean(NoDefault.class));
    }

    @Test
    void scanningAnUnknownPackageIsHarmless() throws Exception {
        Orchestrator o = new Orchestrator("does.not.exist");
        assertDoesNotThrow(o::run);
    }

    @Test
    void concreteClassOutsideScannedPackageCanStillBeCreated() throws Exception {
        Orchestrator o = new Orchestrator("does.not.exist");
        assertNotNull(o.getBean(TypeA.class));
    }
}
