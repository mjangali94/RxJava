package io.reactivex.rxjava3.core.clusters;

public class Cluster_632 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableBlockingTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableUsingTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDematerializeTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromArrayTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastOneTest._Benchmark _Benchmark_benchmark_6;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableBlockingTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableUsingTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDematerializeTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromArrayTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastOneTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_632() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.firstFgnoredCancelAndOnNext.evaluate();
            this._Benchmark_benchmark_1.payloads.usingWithResourceFactoryError.evaluate();
            this._Benchmark_benchmark_1.payloads.usingWithResourceFactoryErrorDisposeEagerly.evaluate();
            this._Benchmark_benchmark_0.payloads.firstOnError.evaluate();
            this._Benchmark_benchmark_4.payloads.selectorCrash.evaluate();
            this._Benchmark_benchmark_5.payloads.conditionalOneIsNull.evaluate();
            this._Benchmark_benchmark_6.payloads.lastOfOneReturnsLast.evaluate();
        }

   }

}