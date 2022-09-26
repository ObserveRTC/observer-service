
export class POJO {
    static from(schema, generateBuilder = false, uuidFields = undefined) {
        const result = new POJO();
        result._doc = schema.doc;
        result._name = schema.name;
        if (!!uuidFields) {
            result._uuidFields = uuidFields;
        }
        result.generateBuilder = generateBuilder;
        for (const field of schema.fields) {
            try {
                result.add(field);
            } catch (err) {
                console.warn(`Error occurred while adding`, field, err);
            }
        }
        return result;
    }
    constructor(level = 0) {
        this._name = undefined;
        this._doc = undefined;
        this._level = level;
        this._version = undefined;
        this._fields = [];
        this._nestedClasses = [];
        this._builderFields = null;
        this._uuidFields = new Set();
        this._assertations = [];
        this._assigns = [];
        this._mongoLines = [];
    }

    get name() {
        return this._name;
    }

    set version(value) {
        this._version = value;
    }

    set generateBuilder(value) {
        this._builderFields = !!value ? [] : null;
    }

    set level(value) {
        this._level = value;
    }

    addUuidField(...fieldNames) {
        this._uuidFields.add(...fieldNames);
    }

    add(field) {
        const types = field.type;
        let type;
        if (typeof types === 'string') {
            type = types;
        } else if (types.length === 2) {
            type = types[1];
        }
        let isObject = typeof type === "object";
        let isArray = isObject && type.type === "array";
        if (isArray) {
            type = type.items;
        }
        isObject = typeof type === "object";
        if (isObject && type.type === "enum") {
            this._addField({
                doc: field.doc ? field.doc : "" + "Possible values: " + type.symbols.join(", "),
                name: field.name,
                type: "String",
            });
            return;
        }
        if (isObject) {
            const nestedClass = POJO.from(type, !!this._builderFields, this._uuidFields);
            nestedClass.level = this._level + 1;
            // console.log("nestedBuilder", this._builderFields, !!this._builderFields);
            this._nestedClasses.push(nestedClass);
            this._addField({
                doc: field.doc ? field.doc : type.doc,
                name: field.name,
                type: nestedClass.name,
                isArray,
            });
            return;
        }
        let javaType = undefined;
        if (this._uuidFields.has(field.name)) {
            javaType = "UUID";
        } else {
            javaType = this._mapPrimitive(type);
        }

        this._addField({
            doc: field.doc,
            name: field.name,
            type: javaType,
            isArray,
        });
    }

    _mapPrimitive(type) {
        switch (type) {
            case "string":
                return "String";
            case "float":
                return "Float";
            case "double":
                return "Double";
            case "long":
                return "Long";
            case "boolean":
                return "Boolean";
            case "int":
                return "Integer";
            case "bytes":
                return "byte[]";
        }
        return undefined;
    }

    _addField({ doc, name, type, isArray }) {
        const writtenType = type + (isArray ? "[]" : "");
        const lines = [
            `\t/**`,
            `\t* ${doc}`,
            `\t*/`,
            `\t@JsonProperty("${name}")`,
            `\tpublic ${writtenType} ${name};`
        ];

        this._fields.push(...lines);
        if (this._builderFields) {
            const methodName = `set` + name.charAt(0).toUpperCase() + name.slice(1);
            const lines = [
                // `\tpublic Builder ${methodName}(${writtenType} value) {`,
                // `\t\tthis.result.${name} = value;`,
                // `\t}`
                `\tpublic Builder ${methodName}(${writtenType} value) {`,
                `\t\tthis.result.${name} = value;`,
                `\t\treturn this;`,
                `\t}`,
            ];
            this._builderFields.push(...lines);
        }
        const mongoLine = `.append("${name}", reportPayload.${name})`;
        this._mongoLines.push(mongoLine);

        // const assertation = `Assertions.assertEquals(expected.${name}, actual.${name}, "${name} field");`
        const methodName = `set` + name.charAt(0).toUpperCase() + name.slice(1);
        const assertation = `.${methodName}(${this._name.charAt(0).toLowerCase()}${this._name.slice(1).replace("Report", "")}.${name})`
        this._assertations.push(assertation);

        const varName =  this.name.charAt(0).toLowerCase() + this.name.slice(1);
        const assign = `${varName}.${name} = ASSIGNED;`
        this._assigns.push(assign);
    }

    drainAssertions() {
        const result = [];
        result.push(
            ``,
            this._name,
            ...this._assertations,
        )
        for (const nestedClass of this._nestedClasses) {
            const assertations = nestedClass.drainAssertions();
            result.push(
                ...assertations
            );
        }
        return result;
    }

    drainAssigns() {
        const result = [];
        result.push(
            ``,
            this.name,
            ...this._assigns,
        )
        for (const nestedClass of this._nestedClasses) {
            const assigns = nestedClass.drainAssigns();
            result.push(
                ...assigns
            );
        }
        return result;
    }

    drainMongoLines() {
        const result = [];
        result.push(
            ``,
            `var document = new Document()`,
            ...this._mongoLines.map(line => `\t${line}`),
        )
        for (const nestedClass of this._nestedClasses) {
            const assigns = nestedClass.drainAssigns();
            result.push(
                ...assigns
            );
        }
        return result;
    }

    toLines() {
        const classKind = this._level < 1 ? " " : " static ";
        const result = [
            `/**`,
            `* ${this._doc}`,
            `*/`,
            `@JsonIgnoreProperties(ignoreUnknown = true)`,
            `public${classKind}class ${this._name} {`,
        ];
        if (this._version) {
            result.push(`\tpublic static final String VERSION="${this._version}";`);
        }
        const builderClassName =`Builder`;
        if (this._builderFields) {
            const lines = [
                `\tpublic static ${builderClassName} newBuilder() {`,
                `\t\treturn new ${builderClassName}();`,
                `\t}`,
            ];
            result.push(...lines);
        }
        for (const nestedClass of this._nestedClasses) {
            for (const nestedLine of nestedClass.toLines()) {
                result.push(`\t${nestedLine}`);
            }
        }
        for (const field of this._fields) {
            result.push(field);
        }
        if (this._builderFields) {
            const lines = [
                `\n`,
                `\tpublic static class ${builderClassName} {`,
                ``,
                `\t\tprivate ${this._name} result = new ${this._name}();`,
                ``,
                ...this._builderFields.map(l => `\t${l}`),
                `\t\tpublic ${this._name} build() {`,
                `\t\t\treturn this.result;`,
                `\t\t}`,
                `\t}`
            ];
            result.push(...lines);
        }
        result.push(`}`);
        return result;
    }
}