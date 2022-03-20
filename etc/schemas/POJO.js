
export class POJO {
    static from(schema, generateBuilder = false) {
        const result = new POJO();
        result._doc = schema.doc;
        result._name = schema.name;
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
            const nestedClass = POJO.from(type, !!this._builderFields);
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
        switch (type) {
            case "string":
                javaType = "String";
                break;
            case "float":
                javaType = "Float";
                break;
            case "double":
                javaType = "Double";
                break;
            case "long":
                javaType = "Long";
                break;
            case "boolean":
                javaType = "Boolean";
                break;
            case "int":
                javaType = "Integer";
                break;
            case "bytes":
                javaType = "byte[]";
                break;
        }
        this._addField({
            doc: field.doc,
            name: field.name,
            type: javaType,
            isArray,
        });
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
                `\tpublic Builder ${methodName}(${writtenType} value) { this.result.${name} = value; return this; }`,
            ];
            this._builderFields.push(...lines);
        }
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