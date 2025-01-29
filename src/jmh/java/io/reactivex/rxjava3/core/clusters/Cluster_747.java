package io.reactivex.rxjava3.core.clusters;

public class Cluster_747 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.util.QueueDrainHelperTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.observers.DeferredScalarObserverTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableResourceWrapperTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservablePublishTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDematerializeTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableAndThenCompletableTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableCollectTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.jdk8.CompletableToCompletionStageTest._Benchmark _Benchmark_benchmark_9;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.util.QueueDrainHelperTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.observers.DeferredScalarObserverTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableResourceWrapperTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservablePublishTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDematerializeTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.completable.CompletableAndThenCompletableTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.observable.ObservableCollectTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.jdk8.CompletableToCompletionStageTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_747() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.observerCheckTerminatedDelayErrorNonEmpty.evaluate();
            this._Benchmark_benchmark_1.payloads.complete.evaluate();
            this._Benchmark_benchmark_2.payloads.onErrorDisposes.evaluate();
            this._Benchmark_benchmark_3.payloads.delayedUpstreamOnSubscribe.evaluate();
            this._Benchmark_benchmark_4.payloads.nonNotificationInstanceAfterDispose.evaluate();
            this._Benchmark_benchmark_1.payloads.nonfusedEmpty.evaluate();
            this._Benchmark_benchmark_0.payloads.observerCheckTerminatedDelayErrorEmpty.evaluate();
            this._Benchmark_benchmark_7.payloads.doubleOnSubscribe.evaluate();
            this._Benchmark_benchmark_8.payloads.collectorFailureDoesNotResultInErrorAndCompletedEmissionsObservable.evaluate();
            this._Benchmark_benchmark_9.payloads.doubleOnSubscribe.evaluate();
            this._Benchmark_benchmark_1.payloads.fusedEmpty.evaluate();
            this._Benchmark_benchmark_0.payloads.observerCheckTerminatedDelayErrorEmptyResource.evaluate();
        }

   }

}