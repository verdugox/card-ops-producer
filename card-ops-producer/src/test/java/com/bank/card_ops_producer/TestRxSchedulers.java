package com.bank.card_ops_producer;

import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/** Fuerza RxJava a ejecutarse de forma determinista en tests. */
public class TestRxSchedulers {
    @BeforeAll
    static void overrideSchedulers() {
        RxJavaPlugins.setInitIoSchedulerHandler(__ -> Schedulers.trampoline());
        RxJavaPlugins.setInitComputationSchedulerHandler(__ -> Schedulers.trampoline());
        RxJavaPlugins.setInitNewThreadSchedulerHandler(__ -> Schedulers.trampoline());
        RxJavaPlugins.setIoSchedulerHandler(__ -> Schedulers.trampoline());
        RxJavaPlugins.setComputationSchedulerHandler(__ -> Schedulers.trampoline());
        RxJavaPlugins.setNewThreadSchedulerHandler(__ -> Schedulers.trampoline());
    }
    @AfterAll
    static void resetSchedulers() { RxJavaPlugins.reset(); }
}
