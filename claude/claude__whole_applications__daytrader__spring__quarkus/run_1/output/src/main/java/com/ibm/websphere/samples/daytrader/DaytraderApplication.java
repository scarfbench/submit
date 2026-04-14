package com.ibm.websphere.samples.daytrader;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class DaytraderApplication {
    public static void main(String[] args) {
        Quarkus.run(args);
    }
}
