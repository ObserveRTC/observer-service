import * as schemas from "@observertc/schemas";
import fs from "fs";
import {JavaClass} from "./JavaClass.js";

const copy = (schema) => {
    const path = "../../src/main/avro-schemas/" + schema.name + ".avsc";
    const text = JSON.stringify(schema, null, 2);
    fs.writeFileSync(path, text);
}

const main = () => {
    const samplesSchema = schemas.AvroSamples;
    const samplesClass = JavaClass.from(samplesSchema);
    // const samplesClass = new JavaClass(samplesSchema.name, samplesSchema.doc);
    samplesClass.version = schemas.version;
    const samplesClassString = samplesClass.toLines().join("\n");
    // console.log(samplesClassString);

    const samplesModule = [
        `package org.observertc.webrtc.schemas.samples;`,
        ``,
        `import com.fasterxml.jackson.annotation.JsonIgnoreProperties;`,
        `import com.fasterxml.jackson.annotation.JsonProperty;`,
        ``,
        samplesClassString
    ].join("\n");
    fs.writeFileSync("./Samples.java", samplesModule);
    for (const key of Object.keys(schemas)) {
        if (!key.startsWith("Avro") || key === "AvroSamples") continue;
        const schema = schemas[key];
        copy(schema);
    }
};

main();