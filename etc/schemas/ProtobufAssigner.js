
function makeMethodName(prefix, name, postfix) {
    return prefix + name.charAt(0).toUpperCase() + name.slice(1) + (postfix ? postfix : "");
}

export class ProtobufAssigner {
    static from(schema, srcName, dstName, uuidFields, level = 0) {
        const result = new ProtobufAssigner();
        result._className = schema.name;
        result._srcName = srcName;
        result._dstName = dstName;
        result._uuidFields = new Set(uuidFields ?? []);
        result._level = level;
        for (const field of schema.fields) {
            try {
                result.add(field);
            } catch (err) {
                console.warn(`Error occurred while adding`, field, err);
            }
        }
        // result._srcName = name.charAt(0).toLowerCase() + name.slice(1);
        return result;
    }
    constructor(level = 0) {
        this._className = undefined;
        this._level = level;
        this._fields = [];
        this._nestedClasses = [];
        this._uuidFields = new Set();
        this._srcName = undefined;
        this._dstName = undefined;
    }


    set level(value) {
        this._level = value;
    }

    add(field) {
        const fieldName = field.name;
        const types = field.type;
        let required = true;
        let type;
        if (typeof types === 'string') {
            type = types;
        } else if (types.length === 2) {
            type = types[1];
            required = false;
        }
        let isObject = typeof type === "object";
        let isArray = isObject && type.type === "array";
        if (isArray) {
            type = type.items;
        }
        isObject = typeof type === "object";
        const hasField = makeMethodName("has", fieldName, "()");
        const getField = makeMethodName("get", fieldName, "()");
        const listPostfix = (fieldName.charAt(fieldName.length - 1) !== 's' ? 's' : '') + "List()"
        const getList = makeMethodName("get", fieldName, listPostfix);
        const countPostfix = (fieldName.charAt(fieldName.length - 1) !== 's' ? 's' : '') + "Count()"
        const getListCount = makeMethodName("get", fieldName, countPostfix)
        const makeAssignee = (fieldName, base) => this._uuidFields.has(fieldName) ? `UUIDAdapter.toStringOrNull(${base})`: `${base}`;
        if (isObject && type.type === "enum") {
            const assignee = makeAssignee(fieldName, `${this._srcName}.${getField}`);
            if (required) {
                this._fields.push(
                    `\t${this._dstName}.${fieldName} = ${assignee};`,
                );
            } else {
                this._fields.push(
                    `if (${this._srcName}.${hasField}) {`,
                    `\t${this._dstName}.${fieldName} = ${assignee};`,
                    `}`
                );
            }
            return;
        }
        if (isObject) {
            // const nestedDstName = isArray ? (`dstItem` + this._level) : `${this._dstName}.${fieldName}`;
            // const nestedSrcName = isArray ? (`srcItem` + this._level) :` ${this._srcName}.${fieldName}`;
            const nestedDstName = `dstItem` + this._level;
            const nestedSrcName = `srcItem` + this._level;
            const nestedClass = ProtobufAssigner.from(type, nestedSrcName, nestedDstName, this._uuidFields, this._level + 1);
            const className = nestedClass._className
            if (isArray) {
                const indexName = `${fieldName}Index`;
                this._fields.push(
                    `if (0 < ${this._srcName}.${getListCount}) {`,
                    `\t${this._dstName}.${fieldName} = new ${className}[ ${this._srcName}.${getListCount}];`,
                    `\tvar ${indexName} = 0;`,
                    `\tfor (var ${nestedSrcName} : ${this._srcName}.${getList}) {`,
                    `\t\tvar ${nestedDstName} = new ${className}();`,
                    ...nestedClass.toLines().map(line => `\t\t${line}`),
                    `\t\t${this._dstName}.${fieldName}[${indexName}++] = ${nestedDstName};`,
                    `\t}`,
                    `}`
                );
            } else {
                this._fields.push(
                    `if (${this._srcName}.${hasField}) {`,
                    `\tvar ${nestedSrcName} = ${this._srcName}.${getField};`,
                    `\tvar ${nestedDstName} = new ${className}();`,
                    ...nestedClass.toLines().map(line => `\t${line}`),
                    `\t${this._dstName}.${fieldName} = ${nestedDstName};`,
                    `}`
                );
            }
            this._nestedClasses.push(nestedClass);
            // this._fields.push()
            return;
        }

        // we have a primitive type
        const assignee = makeAssignee(fieldName, `${this._srcName}.${getField}`);
        if (isArray) {
            const Type = type.charAt(0).toUpperCase() + type.slice(1);
            this._fields.push(
                `if (0 < ${this._srcName}.${getListCount}) {`,
                `\t${this._dstName}.${fieldName} = ${this._srcName}.${getList}.toArray(new ${Type}[0]);`,
                `}`
            );
        } else {
            this._fields.push(
                `if (${this._srcName}.${hasField}) {`,
                `\t${this._dstName}.${fieldName} = ${assignee};`,
                `}`
            );
        }
    }

    toLines() {
        const lines = [];
        // for (const nestedClass of this._nestedClasses) {
        //     for (const nestedLine of nestedClass.toLines()) {
        //         lines.push(`\t${nestedLine}`);
        //     }
        // }
        for (const field of this._fields) {
            lines.push(field);
        }
        const result = [];
        if (this._level < 1) {
            result.push(
                `/** Generated Code, Do not edit! */`,
                ``,
                `package org.observertc.schemas.protobuf;`,
                ``,
                `import org.observertc.observer.common.UUIDAdapter;`,
                `import org.observertc.schemas.protobuf.ProtobufSamples;`,
                `import org.observertc.schemas.samples.Samples;`,
                `import org.observertc.schemas.samples.Samples.*;`,
                `import org.observertc.schemas.samples.Samples.ClientSample.*;`,
                `import org.observertc.schemas.samples.Samples.SfuSample.*;`,
                `import org.observertc.schemas.samples.Samples.TurnSample.*;`,
                ``,
                `import java.util.function.Function;`,
                ``,
                `public class ProtobufSamplesMapper implements Function<ProtobufSamples.Samples, Samples> {`,
                ``,
                `\t@Override`,
                `\tpublic Samples apply(ProtobufSamples.Samples ${this._srcName}) {`,
                `\t\tif (${this._srcName} == null) return null;`,
                `\t\tvar ${this._dstName} = new ${this._className}();`,
                ...lines.map(line => `\t\t${line}`),
                `\t\treturn ${this._dstName};`,
                `\t}`,
                `}`,
            );
        } else {
            result.push(...lines)
        }
        // result.push(`}`);
        return result;
    }
}