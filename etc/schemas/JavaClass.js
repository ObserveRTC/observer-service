
export class JavaClass {
    static from(schema) {
        const result = new JavaClass();
        result._doc = schema.doc;
        result._name = schema.name;
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
    }

    get name() {
        return this._name;
    }

    set version(value) {
        this._version = value;
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
            const nestedClass = JavaClass.from(type);
            nestedClass.level = this._level + 1;
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
        // const field = lines.join("\n");
        this._fields.push(...lines);
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
        for (const nestedClass of this._nestedClasses) {
            for (const nestedLine of nestedClass.toLines()) {
                result.push(`\t${nestedLine}`);
            }
        }
        for (const field of this._fields) {
            result.push(field);
        }
        result.push(`}`);
        return result;
    }
}