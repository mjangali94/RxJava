package io.reactivex.rxjava3.core.clusters;

public class Cluster_994 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableMergeWithSingleTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableRepeatTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSequenceEqualTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.jdk8.SingleFlattenStreamAsFlowableTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapEagerTest._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableMergeWithSingleTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableRepeatTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSequenceEqualTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.jdk8.SingleFlattenStreamAsFlowableTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapEagerTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_994() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.cancel.evaluate();
            this._Benchmark_benchmark_1.payloads.handlerError.evaluate();
            this._Benchmark_benchmark_2.payloads.prefetch.evaluate();
            this._Benchmark_benchmark_3.payloads.fused.evaluate();
            this._Benchmark_benchmark_3.payloads.fusedStreamAvailableLater.evaluate();
            this._Benchmark_benchmark_5.payloads.innerErrorFused.evaluate();
        }

   }

}