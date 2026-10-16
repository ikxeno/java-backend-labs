package io.github.ikxeno;

import io.github.ikxeno.application.LabApplication;

public class Main {
    public static void main(String[] args) {
        System.exit(new LabApplication(System.out, System.err).run(args));
    }
}
