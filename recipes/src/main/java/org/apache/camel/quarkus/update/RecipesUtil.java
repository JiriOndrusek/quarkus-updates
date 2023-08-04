package org.apache.camel.quarkus.update;

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.java.tree.Comment;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JContainer;
import org.openrewrite.java.tree.JRightPadded;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.TextComment;
import org.openrewrite.marker.Markers;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.yaml.tree.Yaml;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.openrewrite.Tree.randomId;

public class RecipesUtil {

    private static String CAMEL_PRESENT_KEY = RecipesUtil.class.getSimpleName();


    //---------------------------- method to access camelPresent flag (provate static for the tests or camel property for real runs)

    public static boolean isCamelPresent(ExecutionContext context) {
        return context.getMessage(CAMEL_PRESENT_KEY, false);
    }

    public static void setCamelPresent(boolean isCamelPresent, ExecutionContext context) {
        context.putMessage(CAMEL_PRESENT_KEY, isCamelPresent);
    }

    //---------------- annotations helpers

    public static J.Annotation createAnnotation(J.Annotation annotation, String name, Function<String, Boolean> argMatcher, String args) {

        LinkedList<Expression> originalArguments = annotation.getArguments() == null ? new LinkedList<>() : new LinkedList(annotation.getArguments());

        String newArgName = args.replaceAll("=.*", "").trim();

        //remove argument with the same name as the new one
        if(argMatcher == null) {
            originalArguments.add(new J.Empty(randomId(), Space.format(args), Markers.EMPTY));
        } else {
            for (ListIterator<Expression> iter = originalArguments.listIterator(); iter.hasNext(); ) {
                Expression expr = iter.next();
                if (argMatcher.apply(expr.toString().replaceAll("\\s", ""))) {
                    iter.set(new J.Empty(randomId(), Space.format(args), Markers.EMPTY));
                }
            }
        }

        //construct arguments for the new annotation
        List<JRightPadded<Expression>> newArgs = new LinkedList<>();
        for(Expression e: originalArguments) {
            newArgs.add(new JRightPadded(e, Space.EMPTY, Markers.EMPTY));
        }

        J.Identifier newAnnotationIdentifier =  new J.Identifier(randomId(), annotation.getPrefix(), Markers.EMPTY, name,
                JavaType.ShallowClass.build("java.lang.Object"), null);
        JContainer<Expression> arguments = JContainer.build(
                Space.EMPTY,
                newArgs,
                Markers.EMPTY);
        return new J.Annotation(UUID.randomUUID(), annotation.getPrefix(), Markers.EMPTY,
                newAnnotationIdentifier, arguments);
    }

    public static Optional<String> getValueOfArgs(List<Expression> expressions, String parameter) {
        if(expressions == null || expressions.isEmpty()) {
            return Optional.empty();
        }
       return expressions.stream()
               .filter(e -> e.toString().replaceAll("\\s", "").startsWith(parameter + "="))
               .map(e -> e.toString().replaceAll("\\s", "").replaceFirst(parameter + "=", ""))
               .findFirst();
    }

    //-------------- methods helping with comments ----

    public static Comment createMultinlineComment(String text) {
        return new TextComment(true, text, null, Markers.EMPTY);
    }
    public static Comment createComment(String text) {
        return new TextComment(false, text, null, Markers.EMPTY);
    }
    public static Xml.Comment createXmlComment(String text) {
        return new Xml.Comment(UUID.randomUUID(), null, Markers.EMPTY, text);
    }

    //--------------- typeCast helper --------------------------------

    public static J createTypeCast(Object type, Expression arg) {
       return new J.TypeCast(
                Tree.randomId(),
                Space.EMPTY,
                Markers.EMPTY,
                RecipesUtil.createParentheses(type),
                arg);
    }

    // -------------------- other helper methods

    public static <T> J.ControlParentheses createParentheses(T t) {
        return new J.ControlParentheses(
                Tree.randomId(),
                Space.EMPTY,
                Markers.EMPTY,
                padRight(t));
    }

    public static J.Identifier createIdentifier(Space prefix, String name, String type) {
       return new J.Identifier(randomId(), prefix, Markers.EMPTY, name,
                JavaType.ShallowClass.build(type), null);
    }

    private static <T> JRightPadded<T> padRight(T tree) {
        return new JRightPadded<>(tree, Space.EMPTY, Markers.EMPTY);
    }

    public static String getProperty(Cursor cursor) {
        StringBuilder asProperty = new StringBuilder();
        Iterator<Object> path = cursor.getPath();
        int i = 0;
        while (path.hasNext()) {
            Object next = path.next();
            if (next instanceof Yaml.Mapping.Entry) {
                Yaml.Mapping.Entry entry = (Yaml.Mapping.Entry) next;
                if (i++ > 0) {
                    asProperty.insert(0, '.');
                }
                asProperty.insert(0, entry.getKey().getValue());
            }
            if (next instanceof Xml.Tag) {
                Xml.Tag t = (Xml.Tag) next;
                if (i++ > 0) {
                    asProperty.insert(0, '/');
                }
                asProperty.insert(0, t.getName());
            }
        }
        return asProperty.toString();
    }

    public enum Category {
        DATAMINING("datamining"),
        AI("ai"),
        API("api"),
        AZURE("azure"),
        BATCH("batch"),
        BIGDATA("bigdata"),
        BITCOIN("bitcoin"),
        BLOCKCHAIN("blockchain"),
        CACHE("cache"),
        CHAT("chat"),
        CLOUD("cloud"),
        CLUSTERING("clustering"),
        CMS("cms"),
        COMPUTE("compute"),
        COMPUTING("computing"),
        CONTAINER("container"),
        CORE("core"),
        CRM("crm"),
        DATA("data"),
        DATABASE("database"),
        DATAGRID("datagrid"),
        DEEPLEARNING("deeplearning"),
        DEPLOYMENT("deployment"),
        DOCUMENT("document"),
        ENDPOINT("endpoint"),
        ENGINE("engine"),
        EVENTBUS("eventbus"),
        FILE("file"),
        HADOOP("hadoop"),
        HCM("hcm"),
        HL7("hl7"),
        HTTP("http"),
        IOT("iot"),
        IPFS("ipfs"),
        JAVA("java"),
        LDAP("ldap"),
        LEDGER("ledger"),
        LOCATION("location"),
        LOG("log"),
        MAIL("mail"),
        MANAGEMENT("management"),
        MESSAGING("messaging"),
        MLLP("mllp"),
        MOBILE("mobile"),
        MONITORING("monitoring"),
        NETWORKING("networking"),
        NOSQL("nosql"),
        OPENAPI("openapi"),
        PAAS("paas"),
        PAYMENT("payment"),
        PLANNING("planning"),
        PRINTING("printing"),
        PROCESS("process"),
        QUEUE("queue"),
        REACTIVE("reactive"),
        REPORTING("reporting"),
        REST("rest"),
        RPC("rpc"),
        RSS("rss"),
        SAP("sap"),
        SCHEDULING("scheduling"),
        SCRIPT("script"),
        SEARCH("search"),
        SECURITY("security"),
        SERVERLESS("serverless"),
        SHEETS("sheets"),
        SOAP("soap"),
        SOCIAL("social"),
        SPRING("spring"),
        SQL("sql"),
        STREAMS("streams"),
        SUPPORT("support"),
        SWAGGER("swagger"),
        SYSTEM("system"),
        TCP("tcp"),
        TESTING("testing"),
        TRANSFORMATION("transformation"),
        UDP("udp"),
        VALIDATION("validation"),
        VOIP("voip"),
        WEBSERVICE("webservice"),
        WEBSOCKET("websocket"),
        WORKFLOW("workflow");
    
        private final String value;
    
        Category(final String value) {
            this.value = value;
        }
    
        /**
         * Returns the string representation of this value
         * 
         * @return Returns the string representation of this value
         */
        public String getValue() {
            return this.value;
        }
    }

}
