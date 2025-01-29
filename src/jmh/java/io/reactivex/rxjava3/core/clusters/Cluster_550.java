package io.reactivex.rxjava3.core.clusters;

public class Cluster_550 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableAmbTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableMergeIterableTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.schedulers.SchedulerPoolFactoryTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.maybe.MaybeTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableAmbTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.completable.CompletableMergeIterableTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.schedulers.SchedulerPoolFactoryTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.maybe.MaybeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_550() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.ambArraySingleElement.evaluate();
            this._Benchmark_benchmark_1.payloads.dispose.evaluate();
            this._Benchmark_benchmark_2.payloads.boolPropertiesFailureReturnsDefaultMissing.evaluate();
            this._Benchmark_benchmark_2.payloads.utilityClass.evaluate();
            this._Benchmark_benchmark_4.payloads.fromFlowableMany.evaluate();
            this._Benchmark_benchmark_0.payloads.disposed.evaluate();
        }

   }

}