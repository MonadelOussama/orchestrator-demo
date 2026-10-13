package com.demo;

public class ClientA {

    @Inject
    private TypeA objectA;

    public TypeA getObjectA() { return objectA; }
    public void setObjectA(TypeA objectA) { this.objectA = objectA; }

    public String callObjA() {
        return this.objectA.hello();
    }
}
