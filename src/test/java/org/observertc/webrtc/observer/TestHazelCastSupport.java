package org.observertc.webrtc.observer;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestHazelCastSupport {
    static int i = 0;

    class A {
        Integer n = ++i;
        String str = "";
    }
    class B implements Observer<A> {

        @Override
        public void onSubscribe(@NonNull Disposable d) {
            System.out.println("onSubscribe");
        }

        @Override
        public void onNext(@NonNull A a) {
            System.out.println("B:" + a.n);
        }

        @Override
        public void onError(@NonNull Throwable e) {

        }

        @Override
        public void onComplete() {

        }
    }
    @Test
    public void t() throws InterruptedException {
        ExecutorService service = new ThreadPoolExecutor(10, 100, 10000, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());
        Subject<A> subject = PublishSubject.create();
        new Thread(() -> {
            for (int i = 0; i < 10; ++i) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                A a = new A();
                subject.onNext(a);
            }
        }).start();
        subject
                .subscribe(i -> {
                    System.out.println("t1: " + i.n);
                });

        AtomicBoolean restarted = new AtomicBoolean(false);
        subject
                .subscribe(i -> {
                    if (restarted.compareAndSet(false, true))
                        throw new RuntimeException();
                    System.out.println("t2: " + i.n);
                });

        subject
                .subscribe(new B());

        Thread.sleep(15000);
    }
}
