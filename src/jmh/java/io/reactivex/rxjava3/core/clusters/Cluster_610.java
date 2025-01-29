package io.reactivex.rxjava3.core.clusters;

public class Cluster_610 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.jdk8.ObservableFromStreamTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableGenerateTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.jdk8.ObservableMapOptionalTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableReplayTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.jdk8.ObservableFlatMapStreamTest._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.jdk8.ObservableFromStreamTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableGenerateTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.jdk8.ObservableMapOptionalTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableReplayTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.jdk8.ObservableFlatMapStreamTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_610() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.streamOfNull.evaluate();
            this._Benchmark_benchmark_0.payloads.streamOfNullConditional.evaluate();
            this._Benchmark_benchmark_2.payloads.multipleOnNext.evaluate();
            this._Benchmark_benchmark_3.payloads.crashDropsOnNexts.evaluate();
            this._Benchmark_benchmark_4.payloads.replaySelectorReturnsNull.evaluate();
            this._Benchmark_benchmark_5.payloads.eventsIgnoredAfterDispose.evaluate();
        }

   }

}