import * as schemas from "@observertc/schemas";
import fs from "fs";
import {POJO} from "./POJO.js";

const copyAvroSchema = (schema) => {
    const path = "../../src/main/avro-schemas/" + schema.name + ".avsc";
    const text = JSON.stringify(schema, null, 2);
    fs.writeFileSync(path, text);
}

const createSamples = (path) => {
    const samplesSchema = schemas.AvroSamples;
    const assigns = [];
    const uuidFields = new Set([
        "callId",
        "clientId",
        "peerConnectionId",
        "trackId",
        "streamId",
        "sinkId",
        "sfuStreamId",
        "sfuSinkId",
        "sfuId",
        "transportId",
        "padId",
        "channelId",
    ]);
    const samplesClass = POJO.from(samplesSchema, true, uuidFields);
    samplesClass.version = schemas.version;
    const samplesClassString = samplesClass.toLines().join("\n");

    const samplesModule = [
        `package org.observertc.schemas.samples;`,
        ``,
        `import com.fasterxml.jackson.annotation.JsonIgnoreProperties;`,
        `import com.fasterxml.jackson.annotation.JsonProperty;`,
        `import java.util.UUID;`,
        ``,
        samplesClassString
    ].join("\n");
    fs.writeFileSync(path, samplesModule);

    assigns.push(...samplesClass.drainAssigns());
    fs.writeFileSync(`samples_assigns.txt`, assigns.join(`\n`));
}

const createReports = (path) => {
    const schemaKeys = Object.keys(schemas);
    const assertations = [];
    for (const schemaKey of schemaKeys) {
        if (!schemaKey.startsWith("Avro")) continue;
        if (!schemaKey.endsWith("Report")) continue;
        if (schemaKey === "AvroReport") continue;
        const schema = schemas[schemaKey];
        const klass = POJO.from(schema, true);
        klass.version = schemas.version;
        const klassString = klass.toLines().join("\n");

        const module = [
            `package org.observertc.schemas.reports;`,
            ``,
            `import com.fasterxml.jackson.annotation.JsonIgnoreProperties;`,
            `import com.fasterxml.jackson.annotation.JsonProperty;`,
            ``,
            klassString
        ].join("\n");
        fs.writeFileSync(path + `${schema.name}.java`, module);

        assertations.push(...klass.drainAssertions());
    }
    fs.writeFileSync(`report_assertations.txt`, assertations.join(`\n`));
}

const main = () => {
    // copy avro schema files
    for (const key of Object.keys(schemas)) {
        if (!key.startsWith("Avro") || key === "AvroSamples") continue;
        const schema = schemas[key];
        copyAvroSchema(schema);
    }

    const samplesPath = "./Samples.java";
    createSamples(samplesPath);
    fs.copyFile(samplesPath, `../../src/main/java/org/observertc/schemas/samples/Samples.java`, (err) => {
        if (err) throw err;
    });

    const reportsPath = "./reports/"
    createReports(reportsPath);
    for (const reportFile of fs.readdirSync(reportsPath)) {
        fs.copyFile(reportsPath + reportFile, `../../src/main/java/org/observertc/schemas/reports/${reportFile}`, (err) => {
            if (err) throw err;
        });
    }

};

main();