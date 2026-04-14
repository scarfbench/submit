package com.example.orderspring;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class OrderApplication {

    public static void main(String[] args) {
        Quarkus.run(args);
    }
}
