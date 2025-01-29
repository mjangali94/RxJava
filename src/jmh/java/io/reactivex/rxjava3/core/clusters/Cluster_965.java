package io.reactivex.rxjava3.core.clusters;

public class Cluster_965 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.FlowableSwitchMapMaybeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableCombineLatestTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableMergeWithMaybeTest._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.FlowableSwitchMapMaybeTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableCombineLatestTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableMergeWithMaybeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_965() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.undeliverableUponCancelDelayError.evaluate();
            this._Benchmark_benchmark_1.payloads.combineLatestDelayErrorArrayOfSources.evaluate();
            this._Benchmark_benchmark_1.payloads.combineLatestDelayErrorIterableOfSources.evaluate();
            this._Benchmark_benchmark_3.payloads.cancelMainOnOtherError.evaluate();
        }

   }

}