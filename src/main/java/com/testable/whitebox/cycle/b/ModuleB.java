package com.testable.whitebox.cycle.b;

import com.testable.whitebox.cycle.a.ModuleA;

/** ArchUnit circular dependency trigger — package b → package a. */
public class ModuleB {
    public String ping() {
        return "B->" + ModuleA.name();
    }

    public static String name() {
        return "B";
    }
}
