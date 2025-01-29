package io.reactivex.rxjava3.core.clusters;

public class Cluster_786 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.core.ConverterTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.core.TransformerTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.flowable.FlowableCovarianceTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSkipTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableAmbTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTakeUntilPredicateTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.core.ConverterTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.core.TransformerTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.flowable.FlowableCovarianceTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSkipTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableAmbTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTakeUntilPredicateTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_786() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.flowableConverterThrows.evaluate();
            this._Benchmark_benchmark_1.payloads.flowableGenericsSignatureTest.evaluate();
            this._Benchmark_benchmark_2.payloads.covarianceOfCompose2.evaluate();
            this._Benchmark_benchmark_2.payloads.covarianceOfCompose.evaluate();
            this._Benchmark_benchmark_4.payloads.skipNegativeElements.evaluate();
            this._Benchmark_benchmark_5.payloads.zipWithIterableOneIsNull.evaluate();
            this._Benchmark_benchmark_6.payloads.singleIterable.evaluate();
            this._Benchmark_benchmark_7.payloads.errorIncludesLastValueAsCause.evaluate();
            this._Benchmark_benchmark_1.payloads.observableTransformerThrows.evaluate();
        }

   }

}