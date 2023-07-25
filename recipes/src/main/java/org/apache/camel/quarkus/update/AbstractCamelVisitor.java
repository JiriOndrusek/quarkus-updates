package org.apache.camel.quarkus.update;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Parent of Camel visitors, skips visit methods in case that there is no camel package imported.
 */
public abstract class AbstractCamelVisitor extends JavaIsoVisitor<ExecutionContext> {

    //flag that camel package is imported to the file
    private boolean camel = false;

    private LinkedList<JavaType> implementsList = new LinkedList<>();

    //There is no need to  initialize all patterns at the class start.
    //Map is a cache for created patterns
    //TODO having the map static, may increase performance
    private Map<String, MethodMatcher> methodMatchers = new HashMap();

    @Override
    public final J.Import visitImport(J.Import _import, ExecutionContext context) {
        //if there is at least one import of camel class, the camel recipe should be executed
        if(_import.getTypeName().contains("org.apache.camel")) {
            camel = true;
        }

        if(!camel) {
            //skip recipe if file does not contain camel
            return _import;
        }

        return doVisitImport(_import, context);
    }

    @Override
    public final J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext context) {
        if (classDecl.getImplements() != null && !classDecl.getImplements().isEmpty()) {
            implementsList.addAll(classDecl.getImplements().stream().map(i -> i.getType()).collect(Collectors.toList()));
        }
        return doVisitClassDeclaration(classDecl, context);
    }

    @Override
    public final J.FieldAccess visitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext context) {
        if(!camel) {
            //skip recipe if file does not contain camel
            return fieldAccess;
        }

        return doVisitFieldAccess(fieldAccess, context);
    }

    @Override
    public final J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext context) {
        if(!camel) {
            //skip recipe if file does not contain camel
            return method;
        }

        return doVisitMethodDeclaration(method, context);
    }

    @Override
    public final J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext context) {
        if(!camel) {
            //skip recipe if file does not contain camel
            return method;
        }

        return doVisitMethodInvocation(method, context);
    }

    @Override
    public final J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext context) {
        if(!camel) {
            //skip recipe if file does not contain camel
            return annotation;
        }
        return doVisitAnnotation(annotation, context);
    }


    //-------------------------------- internal methods used by children---------------------------------

    protected  J.Import doVisitImport(J.Import _import, ExecutionContext context) {
        return super.visitImport(_import, context);
     }

     J.ClassDeclaration doVisitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext context) {
        return super.visitClassDeclaration(classDecl, context);
     }

    protected J.FieldAccess doVisitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext context) {
        return super.visitFieldAccess(fieldAccess, context);
    }

    protected J.MethodDeclaration doVisitMethodDeclaration(J.MethodDeclaration method, ExecutionContext context) {
        return super.visitMethodDeclaration(method, context);
    }

    protected J.MethodInvocation doVisitMethodInvocation(J.MethodInvocation method, ExecutionContext context) {
        return super.visitMethodInvocation(method, context);
    }

    J.Annotation doVisitAnnotation(J.Annotation annotation, ExecutionContext context) {
        return super.visitAnnotation(annotation, context);
    }

     // ------------------------------------------ helper methods -------------------------------------------

    LinkedList<JavaType> getImplementsList() {
        return implementsList;
    }

    public MethodMatcher getMethodMatcher(String signature) {
        MethodMatcher matcher = methodMatchers.get(signature);

        if(matcher == null) {
            matcher = new MethodMatcher(signature);
            methodMatchers.put(signature, matcher);
        }

        return matcher;
    }
}
