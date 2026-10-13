package com.demo;

public class Main {

    public static void main(String[] args) {
        // Optional argument: the package to scan (defaults to com.demo)
        String basePackage = args.length > 0 ? args[0] : "com.demo";

        try {
            Orchestrator orchestrator = new Orchestrator(basePackage);
            orchestrator.run();

            ClientA client = orchestrator.getBean(ClientA.class);

            System.out.println("Result      : " + client.callObjA());
            System.out.println("objectA set : " + (client.getObjectA() != null));
            System.out.println("Singleton   : "
                    + (client.getObjectA() == orchestrator.getBean(TypeA.class)));
        } catch (Exception e) {
            System.err.println("Orchestrator failed: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
