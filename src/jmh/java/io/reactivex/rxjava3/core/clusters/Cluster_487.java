package io.reactivex.rxjava3.core.clusters;

public class Cluster_487 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableToXTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.ObservableCollectWithCollectorTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferStrategyTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.jdk8.FlowableMapOptionalTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFilterTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableToXTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.jdk8.ObservableCollectWithCollectorTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferStrategyTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.jdk8.FlowableMapOptionalTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFilterTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_487() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.toFlowableBuffer.evaluate();
            this._Benchmark_benchmark_1.payloads.collectorFinisherCrash.evaluate();
            this._Benchmark_benchmark_2.payloads.overflowError.evaluate();
            this._Benchmark_benchmark_3.payloads.backpressureNone.evaluate();
            this._Benchmark_benchmark_4.payloads.fusedNormal.evaluate();
            this._Benchmark_benchmark_5.payloads.fusedReject.evaluate();
            this._Benchmark_benchmark_3.payloads.allPresent.evaluate();
            this._Benchmark_benchmark_7.payloads.conditionalFastPathCancelExact.evaluate();
            this._Benchmark_benchmark_3.payloads.mixed.evaluate();
            this._Benchmark_benchmark_2.payloads.overflowCrashes.evaluate();
            this._Benchmark_benchmark_3.payloads.allEmptyConditional.evaluate();
        }

   }

}