import * as schemas from "@observertc/schemas";
import fs from "fs";
import {POJO} from "./POJO.js";
import {exec} from 'child_process';
import {ProtobufAssigner} from "./ProtobufAssigner.js";
import {SamplesConverterHelper} from "./SamplesConverterHelper.js";

const copyAvroSchema = (schema) => {
    const path = "../../src/main/avro-schemas/" + schema.name + ".avsc";
    const text = JSON.stringify(schema, null, 2);
    fs.writeFileSync(path, text);
}
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

const createSamplesPojo = (path) => {
    const samplesSchema = schemas.AvroSamples;
    const assigns = [];
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
    fs.writeFileSync(`archive/SamplesAvro_${schemas.version}.avsc`, JSON.stringify(samplesSchema, null, 2));
}

const createReportsPojo = (path) => {
    const schemaKeys = Object.keys(schemas);
    const assertations = [];
    const assigns = [];
    const mongoLines = [];
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
        assigns.push(...klass.drainAssigns());
        mongoLines.push(`\n\n`,klass.name, ...klass.drainMongoLines());
    }
    fs.writeFileSync(`report_assertations.txt`, assertations.join(`\n`));
    fs.writeFileSync(`report_mongoLines.txt`, mongoLines.join(`\n`));
    fs.writeFileSync(`reports_assigns.txt`, assigns.join(`\n`));
}

const main = () => {
    // copy avro schema files
    for (const key of Object.keys(schemas)) {
        if (!key.startsWith("Avro") || key === "AvroSamples") continue;
        const schema = schemas[key];
        // copyAvroSchema(schema);
    }

    const samplesPath = "./Samples.java";
    createSamplesPojo(samplesPath);
    fs.copyFile(samplesPath, `../../src/main/java/org/observertc/schemas/samples/Samples.java`, (err) => {
        if (err) throw err;
    });

    const reportsPath = "./reports/"
    createReportsPojo(reportsPath);
    for (const reportFile of fs.readdirSync(reportsPath)) {
        fs.copyFile(reportsPath + reportFile, `../../src/main/java/org/observertc/schemas/reports/${reportFile}`, (err) => {
            if (err) throw err;
        });
    }
    const protoFile = `./ProtobufSamples.proto`;
    fs.writeFileSync(protoFile, schemas.ProtobufSamples);
    exec(`protoc --java_out=../../src/main/java/ ${protoFile}`, (error, stdout, stderr) => {
        if (error !== null) console.error('exec error: ' + error);
        fs.rm(protoFile, err => {
            if (err) throw err;
        });
    });
    const assigner = ProtobufAssigner.from(schemas.AvroSamples, "source", "result", uuidFields);
    fs.writeFileSync("../../src/main/java/org/observertc/schemas/protobuf/ProtobufSamplesMapper.java", assigner.toLines().join(`\n`));

    const copier = SamplesConverterHelper.from({
        srcClassName: `Samples`,
        dstClassName: `Samples2`,
        srcName: `source`,
        dstName: `dest`,
        srcSchema: JSON.parse(fs.readFileSync(`./archive/SamplesAvro_2.0.0-beta.59.avsc`)),
        dstSchema: JSON.parse(fs.readFileSync(`./archive/SamplesAvro_2.0.0-beta.61.avsc`)),
        level: 0
    });
    const copyLines = copier.toLines().join("\n");
    fs.writeFileSync(`./from200-beta59-to-200-beta61.txt`, copyLines);
    // console.log(protobuf)
};

main();