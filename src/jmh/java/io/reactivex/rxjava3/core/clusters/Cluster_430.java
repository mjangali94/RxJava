package io.reactivex.rxjava3.core.clusters;

public class Cluster_430 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableWindowWithTimeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableMapTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeBlockingSubscribeTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapSingleElementTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatMapSingleTest._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableWindowWithTimeTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableMapTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeBlockingSubscribeTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapSingleElementTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatMapSingleTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_430() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.timeskipJustOverlap.evaluate();
            this._Benchmark_benchmark_0.payloads.timeskipJustSkip.evaluate();
            this._Benchmark_benchmark_2.payloads.errorPassesThruMap2.evaluate();
            this._Benchmark_benchmark_3.runBenchmark(this._Benchmark_benchmark_3.payloads.observerEmpty);
            this._Benchmark_benchmark_4.payloads.flatMapSingleEmpty.evaluate();
            this._Benchmark_benchmark_5.payloads.flatMapSingleElementEmpty.evaluate();
        }

   }

}