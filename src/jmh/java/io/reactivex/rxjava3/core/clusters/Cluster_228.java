package io.reactivex.rxjava3.core.clusters;

public class Cluster_228 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.exceptions.ExceptionsTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrErrorTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrDefaultTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableFromObservableTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.observers.LambdaObserverTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableBlockingTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.jdk8.ObservableFromOptionalTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableIgnoreElementsTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableRangeLongTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.single.SingleDelayTest._Benchmark _Benchmark_benchmark_14;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableOnErrorReturnTest._Benchmark _Benchmark_benchmark_15;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.exceptions.ExceptionsTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrErrorTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrDefaultTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.completable.CompletableFromObservableTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.observers.LambdaObserverTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.observable.ObservableBlockingTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.jdk8.ObservableFromOptionalTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.observable.ObservableIgnoreElementsTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.observable.ObservableRangeLongTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.single.SingleDelayTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.internal.operators.observable.ObservableOnErrorReturnTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_228() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.threadDeathIsThrown.evaluate();
            this._Benchmark_benchmark_1.payloads.lastJust.evaluate();
            this._Benchmark_benchmark_2.payloads.lastJust.evaluate();
            this._Benchmark_benchmark_1.payloads.singleJust.evaluate();
            this._Benchmark_benchmark_2.payloads.singleJust.evaluate();
            this._Benchmark_benchmark_5.payloads.fromObservable.evaluate();
            this._Benchmark_benchmark_6.payloads.onNextThrows.evaluate();
            this._Benchmark_benchmark_2.payloads.firstJust.evaluate();
            this._Benchmark_benchmark_1.payloads.firstJust.evaluate();
            this._Benchmark_benchmark_6.payloads.onSubscribeThrows.evaluate();
            this._Benchmark_benchmark_10.payloads.blockingFirstEmpty.evaluate();
            this._Benchmark_benchmark_11.payloads.hasValue.evaluate();
            this._Benchmark_benchmark_12.payloads.checkDispose.evaluate();
            this._Benchmark_benchmark_13.payloads.countOne.evaluate();
            this._Benchmark_benchmark_14.payloads.withObservableDispose.evaluate();
            this._Benchmark_benchmark_15.payloads.dispose.evaluate();
        }

   }

}