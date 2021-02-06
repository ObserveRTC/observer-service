package org.observertc.webrtc.observer.connectors.transformations;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Predicate;
import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.configbuilders.Builder;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

@Prototype
public class FilterBuilder extends AbstractBuilder implements Builder<Transformation> {

    private static final Logger logger = LoggerFactory.getLogger(FilterBuilder.class);

    @Override
    public Filter build() {
        Config config = this.convertAndValidate(Config.class);
        Filter result = new Filter();

        Arrays.asList(
                this.makeAllowanceFilter(Report::getType, str -> ReportType.valueOf(str), config.reportType),
                this.makeAllowanceFilter(Report::getServiceName, Function.identity(), config.serviceName),
                this.makeAllowanceFilter(Report::getServiceUUID, Function.identity(), config.serviceUUIDs),
                this.makeAllowanceFilter(Report::getMarker, Function.identity(), config.marker)
        ).forEach(result::addPredicate);

        return result;
    }

    private<T> Predicate<Report> makeAllowanceFilter(Function<Report, T> extractor, Function<String, T> converter, Config.AllowanceConfig config) {
        Set<T> includes = this.collect(converter, config.including);
        Set<T> excludes = this.collect(converter, config.excluding);
        if (includes.size() < 1 && excludes.size() < 1) {
            return reportType -> true;
        }
        Predicate<Report> checkExclusion = report -> !excludes.contains(extractor.apply(report));
        Predicate<Report> checkInclusion = report -> includes.contains(extractor.apply(report));
        if (includes.size() < 1){
            return checkExclusion;
        }

        if (excludes.size() < 1) {
            return checkInclusion;
        }
        return report -> {
            return checkInclusion.test(report) && checkExclusion.test(report);
        };
    }

    private<T> Set<T> collect(Function<String, T> converter, List<String> original) {
        if (Objects.isNull(original)) {
            return Collections.unmodifiableSet(new HashSet<>());
        }
        Set<T> result = new HashSet<>();
        for (String source : original) {
            T type;
            try {
                type = converter.apply(source);
            } catch (Exception ex) {
                logger.warn("converted value for " + source +" throws an exception. it will not be added to the allowancefilter", ex);
                continue;
            }
            result.add(type);
        }
        return Collections.unmodifiableSet(result);
    }


    public static class Config {

        public AllowanceConfig reportType = new AllowanceConfig();
        public AllowanceConfig marker = new AllowanceConfig();
        public AllowanceConfig serviceName = new AllowanceConfig();
        public AllowanceConfig serviceUUIDs = new AllowanceConfig();

        public class AllowanceConfig {
            public List<String> including = new ArrayList<>();
            public List<String> excluding = new ArrayList<>();
        }

    }

}
