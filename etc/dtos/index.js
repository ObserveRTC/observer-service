

const protos = "./protos/"
for (const protoFile of fs.readdirSync(protos)) {
    exec(`protoc --java_out=../../src/main/java/ ${protoFile}`, (error, stdout, stderr) => {
        if (error !== null) console.error('exec error: ' + error);
    });
}
