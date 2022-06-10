package org.observertc.observer.sinks;

import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.Builder;
import org.observertc.observer.configs.ReportsConfig;
import org.observertc.observer.reports.Report;
import org.observertc.observer.reports.ReportTypeVisitors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SinkBuilder extends AbstractBuilder {
    private static final Logger logger = LoggerFactory.getLogger(SinkBuilder.class);
    private final List<String> packages;

    public SinkBuilder() {
        Package thisPackage = this.getClass().getPackage();
        Package[] packages = Package.getPackages();
        this.packages = Arrays.stream(packages)
                .filter(p -> p.getName().startsWith(thisPackage.getName()))
                .map(Package::getName)
                .collect(Collectors.toList());
    }

    public Sink build() {
        Config config = this.convertAndValidate(Config.class);
        String builderClassName = AbstractBuilder.getBuilderClassName(config.type);
        Optional<Builder> builderHolder = this.tryInvoke(builderClassName);
        if (!builderHolder.isPresent()) {
            logger.error("Cannot find sink builder for {} in packages: {}", config.type, String.join(",", this.packages ));
            return null;
        }
        Builder<Sink> sinkBuilder = (Builder<Sink>) builderHolder.get();
        sinkBuilder.withConfiguration(config.config);


        var result = sinkBuilder.build();
        result.setEnabled(config.enabled);
        if (config.reports != null) {
            var reportTypeVisitor = ReportTypeVisitors.makeTypeFilter(config.reports);
            Predicate<Report> filter = report -> reportTypeVisitor.apply(null, report.type);
            result.setReportsFilter(filter);
        }

        return result;
    }

    public static class Config {

        @NotNull
        public String type;

        public ReportsConfig reports = null;

        public boolean enabled = true;

        public Map<String, Object> config;

    }
}
