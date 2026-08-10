package com.testable.whitebox.cycle.a;

import com.testable.whitebox.cycle.b.ModuleB;

/** ArchUnit circular dependency trigger — package a → package b. */
public class ModuleA {
    public String ping() {
        return "A->" + ModuleB.name();
    }

    public static String name() {
        return "A";
    }
}
