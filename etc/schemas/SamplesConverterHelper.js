
function makeMethodName(prefix, name, postfix) {
    return prefix + name.charAt(0).toUpperCase() + name.slice(1) + (postfix ? postfix : "");
}

export class SamplesConverterHelper {
    static from({srcClassName, srcSchema, srcName, dstClassName, dstSchema, dstName, level}) {
        const result = new SamplesConverterHelper(level);
        result._srcClassName = srcClassName;
        result._dstClassName = dstClassName;
        result._srcName = srcName;
        result._dstName = dstName;
        result._level = level;
        // console.log(srcClassName, srcSchema, srcName, dstClassName, dstSchema, dstName, level);
        const srcFields = new Map();
        for (const srcField of srcSchema.fields) {
            srcFields.set(srcField.name, srcField);
        }
        for (const dstField of dstSchema.fields) {
            const dstFieldName = dstField.name;
            const srcField = srcFields.get(dstFieldName);
            if (!srcField) {
                result._mismatches.push(
                    `Field ${dstFieldName} has not been found in ${srcClassName}`
                );
                continue;
            }
            result.add(srcField, dstField);
        }
        return result;
    }
    constructor(level = 0) {
        this._srcClassName = undefined;
        this._dstClassName = undefined;
        this._level = level;
        this._fields = [];
        this._mismatches = [];
        this._nestedClasses = [];
    }


    set level(value) {
        this._level = value;
    }

    add(srcField, dstField) {
        const fieldName = dstField.name;
        const dstTypes = srcField.type;
        const srcTypes = srcField.type;
        let srcType;
        let dstType;
        if (typeof dstTypes === 'string') {
            if (typeof srcTypes !== 'string') {
                this._mismatches.push(`Type is mismatched between ${this._srcClassName}.${fieldName} and ${this._dstClassName}.${fieldName}`);
                return;
            }
            dstType = dstTypes;
            srcType = dstTypes;

        } else if (dstTypes.length === 2) {
            if (srcTypes.length !== 2) {
                this._mismatches.push(`Type is mismatched between ${this._srcClassName}.${fieldName} and ${this._dstClassName}.${fieldName}`);
                return;
            }
            dstType = dstTypes[1];
            srcType = srcTypes[1];
        }
        let isObject = typeof dstType === "object";
        let isArray = isObject && dstType.type === "array";
        if (isArray) {
            if (typeof srcType !== 'object' || srcType.type !== 'array') {
                this._mismatches.push(`Type is mismatched between ${this._srcClassName}.${fieldName} and ${this._dstClassName}.${fieldName}`);
                return;
            }
            dstType = dstType.items;
            srcType = srcType.items;
        }
        isObject = typeof dstType === "object";
        if (isObject && srcType.type === "enum") {
            this._fields.push(
                ``,
                `THIS IS ENUM`,
                `${this._dstName}.${dstField.name} = ${this._srcName}.${srcField.name};`
            );
            return;
        }
        if (isObject) {
            if (typeof srcType !== 'object') {
                this._mismatches.push(`Type is mismatched between ${this._srcClassName}.${fieldName} and ${this._dstClassName}.${fieldName}`);
                return;
            }
            const nestedSrcName = `src${srcType.name}`;
            const nestedDstName = `dst${dstType.name}`;
            if (isArray) {
                this._fields.push(``, `THIS IS AN ARRAY`);
            }
            const nestedClass = SamplesConverterHelper.from({
                                srcClassName: srcType.name,
                                dstClassName: dstType.name,
                                srcName: nestedSrcName,
                                dstName: nestedDstName,
                                srcSchema: srcType,
                                dstSchema: dstType,
                                level: this._level + 1,
                            });
            nestedClass.level = this._level + 1;
            this._fields.push(...nestedClass._fields);
            this._mismatches.push(...nestedClass.mismatches());
            // console.log("nestedBuilder", this._builderFields, !!this._builderFields);
            this._nestedClasses.push(nestedClass);
            return;
        }
        this._fields.push(
            `${this._dstName}.${dstField.name} = ${this._srcName}.${srcField.name};`
        );
    }

    mismatches() {
        return this._mismatches;
    }

    toLines() {
        const result = [];
        if (this._level < 1) {
            result.push(
                ...this._mismatches,
                ...this._fields,
            );
        }
        // result.push(`}`);
        return result;
    }
}