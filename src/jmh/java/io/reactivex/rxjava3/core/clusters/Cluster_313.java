package io.reactivex.rxjava3.core.clusters;

public class Cluster_313 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.flowable.FlowableStartWithTests._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.BlockingFlowableToIteratorTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromIterableTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSwitchIfEmptyTest._Benchmark _Benchmark_benchmark_6;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.flowable.FlowableStartWithTests._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.BlockingFlowableToIteratorTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromIterableTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSwitchIfEmptyTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_313() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.failingFusedInnerCancelsSource.evaluate();
            this._Benchmark_benchmark_1.payloads.startWithIterable.evaluate();
            this._Benchmark_benchmark_2.payloads.toIterator.evaluate();
            this._Benchmark_benchmark_1.payloads.startWithObservable.evaluate();
            this._Benchmark_benchmark_4.payloads.fusedPoll.evaluate();
            this._Benchmark_benchmark_0.payloads.badRequest.evaluate();
            this._Benchmark_benchmark_6.payloads.switchWhenEmpty.evaluate();
            this._Benchmark_benchmark_4.payloads.fusionClear.evaluate();
        }

   }

}