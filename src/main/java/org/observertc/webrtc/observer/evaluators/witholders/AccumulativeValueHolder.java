//package org.observertc.webrtc.observer.evaluators.witholders;
//
//import io.reactivex.rxjava3.functions.Supplier;
//import org.observertc.webrtc.schemas.reports.Report;
//
//import java.util.Comparator;
//import java.util.Objects;
//import java.util.function.Function;
//
//public class AccumulativeValueHolder<T> implements Function<Report, Boolean>, Supplier<T>{
//
//    final Function<Report, T> extractor;
//    final Comparator<T> cmp;
//    private T actual;
//    private T prev;
//
//    public AccumulativeValueHolder(Function<Report, T> extractor, Comparator<T> cmp) {
//        this.extractor = extractor;
//        this.cmp = cmp;
//    }
//
//    @Override
//    public Boolean apply(Report report) {
//        T value = this.extractor.apply(report);
//        if (Objects.isNull(value)) {
//            return false;
//        }
//        this.prev = this.actual;
//        this.actual = value;
//        return 0 < this.cmp.compare(this.prev, this.actual);
//    }
//
//    @Override
//    public T get() throws Throwable {
//        return this.prev;
//    }
//}
