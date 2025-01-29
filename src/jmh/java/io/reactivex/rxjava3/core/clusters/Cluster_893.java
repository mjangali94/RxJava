package io.reactivex.rxjava3.core.clusters;

public class Cluster_893 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapEagerTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableRepeatTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableWindowWithTimeTest._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapEagerTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableRepeatTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableWindowWithTimeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_893() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.concatEagerIterable.evaluate();
            this._Benchmark_benchmark_1.payloads.noSubsequentSubscriptionDelayErrorIterable.evaluate();
            this._Benchmark_benchmark_1.payloads.noSubsequentSubscriptionIterable.evaluate();
            this._Benchmark_benchmark_0.payloads.capacityHint.evaluate();
            this._Benchmark_benchmark_4.payloads.repeatAndDistinctUnbounded.evaluate();
            this._Benchmark_benchmark_5.payloads.cancellingWindowCancelsUpstreamExactTimeSkip.evaluate();
            this._Benchmark_benchmark_0.payloads.iterableDelayError.evaluate();
        }

   }

}