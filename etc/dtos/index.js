import fs from "fs";
import {exec} from 'child_process';

const protos = "./protos/"
for (const protoFile of fs.readdirSync(protos)) {
    exec(`protoc --java_out=../../src/main/java/ ${protos + "/" + protoFile}`, (error, stdout, stderr) => {
        if (error !== null) console.error('exec error: ' + error);
    });
}
