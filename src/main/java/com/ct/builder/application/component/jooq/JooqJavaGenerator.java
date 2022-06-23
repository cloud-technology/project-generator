package com.ct.builder.application.component.jooq;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.jooq.codegen.GeneratorStrategy.Mode;
import org.jooq.Name;
import org.jooq.codegen.JavaGenerator;
import org.jooq.codegen.JavaWriter;
import org.jooq.meta.ColumnDefinition;
import org.jooq.meta.DataTypeDefinition;
import org.jooq.meta.Database;
import org.jooq.meta.Definition;
import org.jooq.meta.EmbeddableDefinition;
import org.jooq.meta.ParameterDefinition;
import org.jooq.meta.RoutineDefinition;
import org.jooq.meta.SchemaDefinition;
import org.jooq.meta.TableDefinition;
import org.jooq.meta.TypedElementDefinition;
import org.jooq.meta.UDTDefinition;
import org.jooq.meta.UniqueKeyDefinition;
import org.jooq.tools.StringUtils;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import lombok.extern.slf4j.Slf4j;

/**
 * 參考這一支 org.jooq.codegen.JavaGenerator
 * https://github.com/jOOQ/jOOQ/blob/main/jOOQ-codegen/src/main/java/org/jooq/codegen/JavaGenerator.java
 * 
 * 
 * 
 */
@Slf4j
public class JooqJavaGenerator extends JavaGenerator {

    private boolean scala;
    private boolean kotlin;
    private String visibility;

    @Override
    protected void generatePojo(TableDefinition tableUdtOrEmbeddable, JavaWriter out) {
        Path repositoryTempPath = null;
        try {
            // project_6656448105147208192/src/main/java =>
            // project_6656448105147208192/RepositoryTemp
            repositoryTempPath = Files.createDirectories(
                    Path.of(Path.of(getStrategy().getTargetDirectory()).getParent().getParent().getParent().toString(),
                            "RepositoryTemp"));
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        log.debug("JooqJavaGenerator.generatePojo >>");
        final String className = getStrategy().getJavaClassName(tableUdtOrEmbeddable, Mode.POJO);
        final String interfaceName = generateInterfaces()
                ? out.ref(getStrategy().getFullJavaClassName(tableUdtOrEmbeddable, Mode.INTERFACE))
                : "";
        final String superName = out.ref(getStrategy().getJavaClassExtends(tableUdtOrEmbeddable, Mode.POJO));
        List<String> interfaces = out.ref(getStrategy().getJavaClassImplements(tableUdtOrEmbeddable, Mode.POJO));
        log.debug("className={}", className); // classname
        log.debug("interfaceName={}", interfaceName);
        log.debug("superName={}", superName);
        log.debug("getTargetDirectory={}", getStrategy().getTargetDirectory()); // /var/folders/j4/pv0lcnmn04j01rz_b
        log.debug("getTargetPackage={}", getStrategy().getTargetPackage()); // com.ct.infrastructure.repositories
        log.debug("getTargetDirectory={}", getStrategy().getTargetDirectory());

        List<ParameterDefinition> list = tableUdtOrEmbeddable.getParameters();
        for (ParameterDefinition param : list) {
            log.debug("getSource={}", param.getSource());
            log.debug("getInputName={}", param.getInputName());
            log.debug("getOutputName={}", param.getOutputName());
        }

        if (generateInterfaces())
            interfaces.add(interfaceName);

        interfaces.add("Persistable<" + this.getIdJavaType(tableUdtOrEmbeddable, out) + ">");

        final List<String> superTypes = list(superName, interfaces);
        log.debug("superTypes={}", superTypes);

        // inmport 在這裡寫
        printPackage(out, tableUdtOrEmbeddable, Mode.POJO);
        out.println("import lombok.Data;");
        out.println("import lombok.extern.slf4j.Slf4j;");
        out.println("import com.fasterxml.jackson.annotation.JsonIgnore;");
        out.println("import com.fasterxml.jackson.annotation.JsonIgnoreProperties;");
        out.println("import org.springframework.data.annotation.CreatedBy;");
        out.println("import org.springframework.data.annotation.CreatedDate;");
        out.println("import org.springframework.data.annotation.LastModifiedBy;");
        out.println("import org.springframework.data.annotation.LastModifiedDate;");
        out.println("import javax.persistence.EntityListeners;");
        out.println("import org.springframework.data.jpa.domain.support.AuditingEntityListener;");
        out.println("import org.springframework.data.domain.Persistable;");
        out.println("import javax.persistence.Version;");

        

        out.println("");
        // System.out.println("tableUdtOrEmbeddable???");
        // System.out.println(tableUdtOrEmbeddable);
        // System.out.println(tableUdtOrEmbeddable instanceof TableDefinition);

        if (tableUdtOrEmbeddable instanceof TableDefinition)
            generatePojoClassJavadoc((TableDefinition) tableUdtOrEmbeddable, out);
        else if (tableUdtOrEmbeddable instanceof EmbeddableDefinition)
            generateEmbeddableClassJavadoc((EmbeddableDefinition) tableUdtOrEmbeddable, out);
        else
            generateUDTPojoClassJavadoc((UDTDefinition) tableUdtOrEmbeddable, out);

        // @SuppressWarnings 之前
        printClassAnnotations(out, tableUdtOrEmbeddable, Mode.POJO);
        out.println("@Slf4j");
        out.println("@Data");
        out.println("@EntityListeners({AuditingEntityListener.class})");
        out.println("@JsonIgnoreProperties(value = {\"hibernateLazyInitializer\", \"handler\", \"fieldHandler\"})");

        // @Entity 之前
        if (tableUdtOrEmbeddable instanceof TableDefinition)
            printTableJPAAnnotation(out, (TableDefinition) tableUdtOrEmbeddable);

        // 這邊會取出每一個欄位
        int maxLength0 = 0;
        for (TypedElementDefinition<?> column : getTypedElements(tableUdtOrEmbeddable)) {
            maxLength0 = Math.max(maxLength0,
                    out.ref(getJavaType(column.getType(resolver(out, Mode.POJO)), out, Mode.POJO)).length());
            // log.debug("resolver={}", resolver(out, Mode.POJO));
            // log.debug("column.getType={}", column.getType(resolver(out, Mode.POJO)));
            // log.debug("getJavaType={}", getJavaType(column.getType(resolver(out,
            // Mode.POJO)), out, Mode.POJO));
            // java.lang.String
        }
        int maxLength = maxLength0;

        if (scala) {
            out.println("%s%sclass %s(", visibility(), (generatePojosAsScalaCaseClasses() ? "case " : ""), className);

            forEach(getTypedElements(tableUdtOrEmbeddable), (column, separator) -> {
                out.println("%s%s %s: %s%s",
                        visibility(generateInterfaces()),
                        generateImmutablePojos() ? "val" : "var",
                        scalaWhitespaceSuffix(getStrategy().getJavaMemberName(column, Mode.POJO)),
                        out.ref(getJavaType(column.getType(resolver(out, Mode.POJO)), out, Mode.POJO)),
                        separator);
            });

            out.println(")[[before= extends ][%s]][[before= with ][separator= with ][%s]] {", first(superTypes),
                    remaining(superTypes));
        } else if (kotlin) {
            out.println("%s%sclass %s(", visibility(), (generatePojosAsKotlinDataClasses() ? "data " : ""), className);

            forEach(getTypedElements(tableUdtOrEmbeddable), (column, separator) -> {
                final String member = getStrategy().getJavaMemberName(column, Mode.POJO);

                if (column instanceof ColumnDefinition)
                    printColumnJPAAnnotation(out, (ColumnDefinition) column);

                printValidationAnnotation(out, column);
                if (!generateImmutablePojos())
                    printKotlinSetterAnnotation(out, column, Mode.POJO);

                out.println("%s%s%s %s: %s? = null%s",
                        visibility(generateInterfaces()),
                        generateInterfaces() ? "override " : "",
                        generateImmutablePojos() ? "val" : "var",
                        member,
                        out.ref(getJavaType(column.getType(resolver(out, Mode.POJO)), out, Mode.POJO)),
                        separator);
            });

            out.println(")[[before=: ][%s]] {", superTypes);
        } else {
            log.debug("Java Class, generatePojosAsJavaRecordClasses={}", generatePojosAsJavaRecordClasses());
            // Class 之前
            if (generatePojosAsJavaRecordClasses()) {
                out.println("%srecord %s(", visibility(), className);

                forEach(getTypedElements(tableUdtOrEmbeddable), (column, separator) -> {
                    out.println("%s %s%s",
                            StringUtils.rightPad(
                                    out.ref(getJavaType(column.getType(resolver(out, Mode.POJO)), out, Mode.POJO)),
                                    maxLength),
                            getStrategy().getJavaMemberName(column, Mode.POJO),
                            separator);
                    log.debug("{} {}{}",
                            StringUtils.rightPad(
                                    out.ref(getJavaType(column.getType(resolver(out, Mode.POJO)), out, Mode.POJO)),
                                    maxLength),
                            getStrategy().getJavaMemberName(column, Mode.POJO),
                            separator);
                });

                out.println(")[[before= implements ][%s]] {", interfaces);
            } else {
                log.debug("generatePojosAsJavaRecordClasses==false");
                out.println("%sclass %s[[before= extends ][%s]][[before= implements ][%s]] {", visibility(), className,
                        list(superName), interfaces);
                log.debug("visibility()={}", visibility());
                log.debug("className()={}", className);
                log.debug("list(superName)={}", list(superName));
                log.debug("interfaces()={}", interfaces);

            }

            if (generateSerializablePojos() || generateSerializableInterfaces())
                out.printSerial();

            out.println();

            // 這是每一個 field
            if (!generatePojosAsJavaRecordClasses()) {
                for (TypedElementDefinition<?> column : getTypedElements(tableUdtOrEmbeddable)) {
                    // 印出 JPA annotation
                    /**
                     * created_by
                     * created_date
                     * last_modified_by
                     * last_modified_date
                     */
                    if ("created_by".equals(column.getName().toLowerCase())) {
                        out.println("@CreatedBy");
                    }
                    if ("created_date".equals(column.getName().toLowerCase())) {
                        out.println("@CreatedDate");
                    }
                    if ("last_modified_by".equals(column.getName().toLowerCase())) {
                        out.println("@LastModifiedBy");
                    }
                    if ("last_modified_date".equals(column.getName().toLowerCase())) {
                        out.println("@LastModifiedDate");
                    }
                    if ("current_versions".equals(column.getName().toLowerCase())) {
                        out.println("@Version");
                    }
                    ColumnDefinition columnDefinition = (ColumnDefinition) column;
                    UniqueKeyDefinition pk = columnDefinition.getPrimaryKey();
                    if (pk != null) {
                        // 我是主鍵 先記錄下來

                        log.debug("JavaPackageName={}",
                                getStrategy().getJavaPackageName(tableUdtOrEmbeddable, Mode.DEFAULT));
                        // com.ct.infrastructure.repositories.tables
                        log.debug("className={}", className);
                        // Newtable
                        // log.debug("columnDefinition.getName={}", columnDefinition.getName());
                        // column name
                        // log.debug("columnDefinition.getJavaType={}",
                        //         getJavaType(column.getType(resolver(out, Mode.POJO)), out, Mode.POJO));
                        // java.lang.String

                        try {
                            Map<String, String> repositoryVO = new HashMap();
                            repositoryVO.put("packageName",
                                    getStrategy().getTargetPackage());
                            repositoryVO.put("className", className);
                            repositoryVO.put("primaryKeyType",
                                    getJavaType(column.getType(resolver(out, Mode.POJO)), out, Mode.POJO));

                            InputStream inputStream = JooqJavaGenerator.class.getClassLoader()
                                    .getResourceAsStream("templates/repository/JpaRepository.mustache");
                            String templateText = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                            Template template = Mustache.compiler().compile(templateText);
                            String outputContent = template.execute(repositoryVO);

                            // Path repositoriePath = Paths.get(getStrategy().getTargetDirectory() +
                            // File.separator
                            // + getStrategy().getTargetPackage().replaceAll("\\.", File.separator),
                            // className + "Repository.java");
                            // 資料夾不知為何會被清掉, 只好先存再搬
                            Path repositoriePath = Path.of(repositoryTempPath.toString(),
                                    className + "Repository.java");

                            // Path repositoriePath = Path.of(repositoryTempPath.toString(),
                            // className + "Repository.java");
                            log.debug("repositoriePath={}", repositoriePath);
                            Files.writeString(repositoriePath, outputContent, StandardCharsets.UTF_8,
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

                            // log.debug("outputContent={}", outputContent);
                        } catch (IOException e) {
                            log.error("", e);
                        }

                    }

                    printColumnJPAAnnotation(out, (ColumnDefinition) column);
                    printValidationAnnotation(out, column);
                    log.debug("columnDefinition.getName={}", columnDefinition.getName());
                    log.debug("columnDefinition.getJavaType={}",
                                getJavaType(column.getType(resolver(out, Mode.POJO)), out, Mode.POJO));

                    out.println("private %s%s %s;",
                            generateImmutablePojos() ? "final " : "",
                            StringUtils.rightPad(
                                    out.ref(getJavaType(column.getType(resolver(out, Mode.POJO)), out, Mode.POJO)),
                                    maxLength0),
                            getStrategy().getJavaMemberName(column, Mode.POJO));
                    out.println();
                }
            }
        }

        // Constructors
        // ---------------------------------------------------------------------

        // Default constructor
        // if (!generateImmutablePojos() && !generatePojosAsJavaRecordClasses())
        // generatePojoDefaultConstructor(tableUdtOrEmbeddable, out);

        if (!kotlin) {
            if (!generatePojosAsJavaRecordClasses()) {

                // [#1363] [#7055] copy constructor
                // generatePojoCopyConstructor(tableUdtOrEmbeddable, out);

                // Multi-constructor
                // generatePojoMultiConstructor(tableUdtOrEmbeddable, out);
            }

            // List<? extends TypedElementDefinition<?>> elements =
            // getTypedElements(tableUdtOrEmbeddable);
            // for (int i = 0; i < elements.size(); i++) {
            // TypedElementDefinition<?> column = elements.get(i);

            // if (!generatePojosAsJavaRecordClasses() || generateInterfaces())
            // if (tableUdtOrEmbeddable instanceof TableDefinition) {
            // // 每一個 GET
            // generatePojoGetter(column, i, out);
            // } else {
            // generateUDTPojoGetter(column, i, out);
            // }
            // // Setter
            // if (!generateImmutablePojos())
            // if (tableUdtOrEmbeddable instanceof TableDefinition)
            // generatePojoSetter(column, i, out);
            // else
            // generateUDTPojoSetter(column, i, out);
            // }
        }

        if (tableUdtOrEmbeddable instanceof TableDefinition) {
            List<EmbeddableDefinition> embeddables = ((TableDefinition) tableUdtOrEmbeddable)
                    .getReferencedEmbeddables();

            for (int i = 0; i < embeddables.size(); i++) {
                EmbeddableDefinition embeddable = embeddables.get(i);

                if (!generateImmutablePojos())
                    generateEmbeddablePojoSetter(embeddable, i, out);
                generateEmbeddablePojoGetter(embeddable, i, out);
            }
        }

        if (generatePojosEqualsAndHashCode())
            generatePojoEqualsAndHashCode(tableUdtOrEmbeddable, out);

        if (generatePojosToString())
            generatePojoToString(tableUdtOrEmbeddable, out);

        if (generateInterfaces() && !generateImmutablePojos())
            printFromAndInto(out, tableUdtOrEmbeddable, Mode.POJO);

        if (tableUdtOrEmbeddable instanceof TableDefinition)
            generatePojoClassFooter((TableDefinition) tableUdtOrEmbeddable, out);
        else if (tableUdtOrEmbeddable instanceof EmbeddableDefinition)
            generateEmbeddableClassFooter((EmbeddableDefinition) tableUdtOrEmbeddable, out);
        else
            generateUDTPojoClassFooter((UDTDefinition) tableUdtOrEmbeddable, out);

        out.println("}");
        closeJavaWriter(out);

        log.debug("JooqJavaGenerator.generatePojo <<");
    }

    @Override
    protected void generatePojoClassFooter(TableDefinition tableDefinition, JavaWriter out) {
        log.debug("generatePojoClassFooter");
        String isNewColumnName = null;
        String pkColumnName = null;
        for (TypedElementDefinition<?> column : getTypedElements(tableDefinition)) {
            if ("created_date".equals(column.getName().toLowerCase())) {
                isNewColumnName = "createdDate";
            }
            ColumnDefinition columnDefinition = (ColumnDefinition) column;
            UniqueKeyDefinition uniqueKeyDefinition = columnDefinition.getPrimaryKey();
            if (null != uniqueKeyDefinition) {
                pkColumnName = getStrategy().getJavaMemberName(column, Mode.POJO);
            }
        }

        if(StringUtils.isEmpty(isNewColumnName)){
            isNewColumnName = pkColumnName;
        }

        out.println("");
        out.println("@Override");
        out.println("public " + this.getIdJavaType(tableDefinition, out) + " getId() {");
        out.println("return this." + this.getIdName(tableDefinition) + ";");
        out.println("}");
        out.println("");

        
        out.println("@JsonIgnore");
        out.println("@Override");
        out.println("public boolean isNew() {");
        out.println("return null == this." + isNewColumnName + ";");
        out.println("}");
        out.println("");
    }

    private String getIdJavaType(TableDefinition tableDefinition, JavaWriter out) {

        ColumnDefinition columnDefinition = tableDefinition.getPrimaryKey().getKeyColumns().get(0);
        String type = out.ref(getJavaType(columnDefinition.getType(resolver(null, Mode.POJO)), null, Mode.POJO));

        return type;
    }

    private String getIdName(TableDefinition tableDefinition) {

        ColumnDefinition columnDefinition = tableDefinition.getPrimaryKey().getKeyColumns().get(0);
        return columnDefinition.getName();
    }

    private static final <T> List<T> list(T first, List<T> remaining) {
        List<T> result = new ArrayList<>();

        result.addAll(list(first));
        result.addAll(remaining);

        return result;
    }

    private List<? extends TypedElementDefinition<? extends Definition>> getTypedElements(Definition definition) {
        if (definition instanceof TableDefinition)
            return ((TableDefinition) definition).getColumns();
        else if (definition instanceof EmbeddableDefinition)
            return ((EmbeddableDefinition) definition).getColumns();
        else if (definition instanceof UDTDefinition)
            return ((UDTDefinition) definition).getAttributes();
        else if (definition instanceof RoutineDefinition)
            return ((RoutineDefinition) definition).getAllParameters();
        else
            throw new IllegalArgumentException("Unsupported type : " + definition);
    }

    private String visibility(boolean effectivelyPublic) {
        return effectivelyPublic ? visibilityPublic() : visibility();
    }

    private String visibilityPublic() {
        if (scala)
            return "";
        else if (kotlin)
            return "".equals(visibility()) ? visibility() : "public ";
        else
            return "public ";
    }

    private String visibility() {
        if (visibility == null) {
            switch (generateVisibilityModifier()) {
                case NONE:
                    visibility = "";
                    break;
                case PUBLIC:
                    visibility = scala ? "" : "public ";
                    break;
                case INTERNAL:
                    visibility = scala ? "" : kotlin ? "internal " : "public ";
                    break;
                case DEFAULT:
                default:
                    visibility = scala || kotlin ? "" : "public ";
                    break;
            }
        }

        return visibility;
    }

    private final <T> void forEach(Collection<T> list, BiConsumer<? super T, ? super String> definitionAndSeparator) {
        forEach(list, "", ",", definitionAndSeparator);
    }

    private final <T> void forEach(Collection<T> list, String nonSeparator, String separator,
            BiConsumer<? super T, ? super String> definitionAndSeparator) {
        int size = list.size();
        int i = 0;

        for (T d : list)
            definitionAndSeparator.accept(d, i++ < size - 1 ? separator : nonSeparator);
    }

    private static final Pattern P_SCALA_WHITESPACE_SUFFIX = Pattern.compile("^.*[^a-zA-Z0-9]$");

    private String scalaWhitespaceSuffix(String string) {
        return string == null
                ? null
                : P_SCALA_WHITESPACE_SUFFIX.matcher(string).matches()
                        ? (string + " ")
                        : string;
    }

    private static final <T> List<T> first(Collection<T> objects) {
        List<T> result = new ArrayList<>();

        if (objects != null) {
            for (T object : objects) {
                result.add(object);
                break;
            }
        }

        return result;
    }

    private static final <T> List<T> remaining(Collection<T> objects) {
        List<T> result = new ArrayList<>();

        if (objects != null) {
            result.addAll(objects);

            if (result.size() > 0)
                result.remove(0);
        }

        return result;
    }

    private void printValidationAnnotation(JavaWriter out, TypedElementDefinition<?> column) {
        if (generateValidationAnnotations()) {
            String prefix = kotlin ? "get:" : "";
            DataTypeDefinition type = column.getType(resolver(out));

            // [#5128] defaulted columns are nullable in Java
            if (!column.getType(resolver(out)).isNullable() &&
                    !column.getType(resolver(out)).isDefaulted() &&
                    !column.getType(resolver(out)).isIdentity())
                out.println("@%s%s", prefix, out.ref("jakarta.validation.constraints.NotNull"));

            String javaType = getJavaType(type, out);
            if ("java.lang.String".equals(javaType) || "byte[]".equals(javaType)) {
                int length = type.getLength();

                if (length > 0)
                    out.println("@%s%s(max = %s)", prefix, out.ref("jakarta.validation.constraints.Size"), length);
            }
        }
    }

    @SafeVarargs
    private static final <T> List<T> list(T... objects) {
        List<T> result = new ArrayList<>();

        if (objects != null)
            for (T object : objects)
                if (object != null && !"".equals(object))
                    result.add(object);

        return result;
    }

    private void printFromAndInto(JavaWriter out, Definition tableOrUDT, Mode mode) {
        String qualified = out.ref(getStrategy().getFullJavaClassName(tableOrUDT, Mode.INTERFACE));

        out.header("FROM and INTO");
        boolean override = generateInterfaces() && !generateImmutableInterfaces();

        if (scala) {
            out.println();
            out.println("%s%sdef from(from: %s) {", visibilityPublic(), (override ? "override " : ""), qualified);
        } else if (kotlin) {
            out.println();
            out.println("%s%sfun from(from: %s) {", visibilityPublic(), (override ? "override " : ""), qualified);
        } else {
            out.overrideInheritIf(override);
            out.println("%svoid from(%s from) {", visibilityPublic(), qualified);
        }

        for (TypedElementDefinition<?> column : getTypedElements(tableOrUDT)) {
            final String setter = getStrategy().getJavaSetterName(column, Mode.INTERFACE);
            final String getter = getStrategy().getJavaGetterName(column, Mode.INTERFACE);

            // TODO: Use appropriate Mode here
            final String member = getStrategy().getJavaMemberName(column, Mode.POJO);

            if (scala)
                out.println("%s(from.%s)", setter, getter);
            else if (kotlin)
                out.println("%s = from.%s", member, member);
            else
                out.println("%s(from.%s());", setter, getter);
        }

        out.println("}");

        if (override) {
            // [#10191] Java and Kotlin can produce overloads for this method despite
            // generic type erasure, but Scala cannot, see
            // https://twitter.com/lukaseder/status/1262652304773259264

            if (scala) {
                if (mode != Mode.POJO) {
                    out.println();
                    out.println("%soverride def into [E](into: E): E = {", visibilityPublic(), qualified);
                    out.println("if (into.isInstanceOf[%s])", qualified);
                    out.println("into.asInstanceOf[%s].from(this)", qualified);
                    out.println("else");
                    out.println("super.into(into)");
                    out.println("into");
                    out.println("}");
                }
            } else if (kotlin) {
                out.println();
                out.println("%s%sfun <E : %s> into(into: E): E {", visibilityPublic(), (override ? "override " : ""),
                        qualified);
                out.println("into.from(this)");
                out.println("return into");
                out.println("}");
            } else {
                out.overrideInherit();
                out.println("%s<E extends %s> E into(E into) {", visibilityPublic(), qualified);
                out.println("into.from(this);");
                out.println("return into;");
                out.println("}");
            }
        }
    }

    protected void printColumnJPAAnnotation(JavaWriter out, ColumnDefinition column) {
        int indent = out.indent();

        if (generateJPAAnnotations()) {
            String prefix = kotlin ? "get:" : "";
            UniqueKeyDefinition pk = column.getPrimaryKey();

            if (pk != null) { // 主鍵
                if (pk.getKeyColumns().size() == 1) {
                    // Since JPA 1.0
                    out.println("@%s%s", prefix, out.ref("jakarta.persistence.Id"));
                    // 輸出 Id

                    // Since JPA 1.0
                    if (pk.getKeyColumns().get(0).isIdentity())
                        out.println("@%s%s(strategy = %s.IDENTITY)",
                                prefix,
                                out.ref("jakarta.persistence.GeneratedValue"),
                                out.ref("jakarta.persistence.GenerationType"));
                }
            }

            String nullable = "";
            if (!column.getType(resolver(out)).isNullable())
                nullable = ", nullable = false";

            String length = "";
            String precision = "";
            String scale = "";

            if (column.getType(resolver(out)).getLength() > 0) {
                length = ", length = " + column.getType(resolver(out)).getLength();
            } else if (column.getType(resolver(out)).getPrecision() > 0) {
                precision = ", precision = " + column.getType(resolver(out)).getPrecision();

                if (column.getType(resolver(out)).getScale() > 0) {
                    scale = ", scale = " + column.getType(resolver(out)).getScale();
                }
            }

            // [#8535] The unique flag is not set on the column, but only on
            // the table's @UniqueConstraint section.

            // Since JPA 1.0
            out.print("@%s%s(name = \"", prefix, out.ref("jakarta.persistence.Column"));
            out.print(escapeString(column.getName()));
            out.print("\"");
            out.print(nullable);
            out.print(length);
            out.print(precision);
            out.print(scale);
            out.println(")");
        }

        // [#10196] The above logic triggers an indent level of -1, incorrectly
        out.indent(indent);
    }

    private String escapeString(String string) {

        if (string == null)
            return null;

        // [#3450] Escape also the escape sequence, among other things that break Java
        // strings.
        String result = string.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");

        // [#10869] Prevent string interpolation in Kotlin
        if (kotlin)
            result = result.replace("$", "\\$");

        // [#10007] [#10318] Very long strings cannot be handled by the javac compiler.
        int max = 16384;
        if (result.length() <= max)
            return result;

        StringBuilder sb = new StringBuilder("\" + \"");
        for (int i = 0; i < result.length(); i += max) {
            if (i > 0)
                sb.append("\".toString() + \"");

            sb.append(result, i, Math.min(i + max, result.length()));
        }

        return sb.append("\".toString() + \"").toString();
    }

    protected String getType(Database db, SchemaDefinition schema, JavaWriter out, String t, int p, int s, Name u, String javaType, String defaultType, Mode udtMode) {
        String javaObjType = super.getType(db, schema, out, t, p, s, u, javaType, defaultType, udtMode);
        if(javaObjType.equals("java.time.OffsetDateTime")){
            javaObjType = "java.time.Instant";
        }
        return javaObjType;
    }

}
